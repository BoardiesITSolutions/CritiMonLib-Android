

package com.BoardiesITSolutions.CritiMonLib;

import android.os.AsyncTask;
import android.util.Log;
import okhttp3.*;
import okhttp3.internal.http2.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

/**
 * Copyright (C) Chris Board - Boardies IT Solutions
 * August 2019
 * https://critimon.com
 * https://support.boardiesitsolutions.com
 */
public class APIHandler extends AsyncTask<HashMap<String, String>, Void, JSONObject>
{
    protected static final int API_NOT_INITIALISED = 4;
    protected enum API_Call  {Initialise, SendCrash}
    private APIHandler.API_Call apiCall;
    private ICritiMonResultHandler iCritiMonResultHandler = null;
    private static String sessionID = null;
    private static String DOLB = null;
    private HashMap<String, String> postData;
    private IInternalCritiMonResponseHandler iInternalCritiMonResponseHandler;

    public APIHandler(APIHandler.API_Call apiCall)
    {
        this.apiCall = apiCall;
    }

    public APIHandler(APIHandler.API_Call apiCall, ICritiMonResultHandler iCritiMonResultHandler)
    {
        this.apiCall = apiCall;
        this.iCritiMonResultHandler = iCritiMonResultHandler;
    }

    public APIHandler(APIHandler.API_Call apiCall, ICritiMonResultHandler iCritiMonResultHandler, IInternalCritiMonResponseHandler iInternalCritiMonResponseHandler)
    {
        this.apiCall = apiCall;
        this.iCritiMonResultHandler = iCritiMonResultHandler;
        this.iInternalCritiMonResponseHandler = iInternalCritiMonResponseHandler;
    }

    @Override
    protected JSONObject doInBackground(HashMap<String, String>[] lists)
    {

        String serverURL = CritiMon.context.getString(R.string.critimon_url) + "/";

        //If the server URL is just a slash, it means the app hasn't tried to override (only done by Boardies IT Solutions)
        if (serverURL.equals("/"))
        {
            serverURL = "https://engine.critimon.com/";
        }

        HashMap<String, String> postData = lists[0];



        if (apiCall == API_Call.SendCrash && postData.size() == 0)
        {
            throw new NullPointerException("Empty post data while attemtping to send crash");
        }
        switch (apiCall)
        {
            case Initialise:
                serverURL = serverURL + "initialise";
                break;
            case SendCrash:
                serverURL = serverURL + "crash";
                break;
            default:
                Log.e("API_Handler", "Unknown API Call");
                break;
        }

        Log.d("CritiMon", "Using URL: " + serverURL);
        String authorisationToken = "";

        FormBody.Builder formBuilder = new FormBody.Builder();


        Iterator it = postData.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry pair = (Map.Entry)it.next();
            if (pair.getKey().equals("APIKey"))
            {
                authorisationToken = pair.getValue().toString();
            }
            else
            {
                formBuilder.add(pair.getKey().toString(), pair.getValue().toString());
            }


        }

        RequestBody body = formBuilder.build();


        Headers.Builder headerBuilder = new Headers.Builder();
        headerBuilder.add("Authorisation-Token", authorisationToken);
        headerBuilder.add("User-Agent", "CritiMon Android Library");
        if ((sessionID != null) && !sessionID.isEmpty() )
        {
            headerBuilder.add("Cookie", "SESSIONID=" + sessionID);
        }
        if ((DOLB != null && !DOLB.isEmpty()))
        {
            headerBuilder.add("Cookie", "DO-LB=" + DOLB);
        }
        headerBuilder.add("Connection", "close");

        Headers headers = headerBuilder.build();

        Request request = new Request.Builder()
                //.addHeader("Authorisation-Token", authorisationToken)
                //.addHeader("User-Agent", "CritiMon Android Library")
                .headers(headers)
                .url(serverURL)
                .post(body)
                .build();



        OkHttpClient httpClient = new OkHttpClient();

        try
        {
            Response response = httpClient.newCall(request).execute();


            if (apiCall == API_Call.Initialise)
            {
                Headers responseHeaders = response.headers();

                //The server can send back multiple Set-Cookie headers - one by myself and others from cloudflare load balancer and proxy
                //We can't use response.get("Set-Cookie") has that will only get the last header that matches, so potentially can miss the session id
                //so instead loop over the headers looking for the one that contains the SESSIONID
                for (int i = 0; i < responseHeaders.size(); i++)
                {
                    if (responseHeaders.name(i).equalsIgnoreCase("Set-Cookie") && responseHeaders.value(i).contains("SESSIONID"))
                    {
                        setSessionIDFromCookie(responseHeaders.value(i));
                    }
                    else if (responseHeaders.name(i).equalsIgnoreCase("Set-Cookie") && responseHeaders.value(i).contains("DO-LB"))
                    {
                        setDOLBFromCookie(responseHeaders.value(i));
                    }

                    if (sessionID != null && DOLB != null)
                    {
                        break;
                    }
                }
            }

            String responseBody = response.body().string();
            response.close();
            Log.d("CritiMon", "Response: " + responseBody);

            if ((responseBody != null) && !responseBody.isEmpty())
            {
                JSONObject jsonObject = new JSONObject(responseBody);
                if (apiCall == API_Call.SendCrash)
                {
                    if (jsonObject.getInt("result") == 4) //Not initialised response
                    {
                        CritiMon.retryCrashInfoQueue.add(postData);
                    }
                }
                else if (apiCall == API_Call.Initialise) {
                    try {
                        if (jsonObject.getInt("result") == 0) {
                            //If initialisation done, trigger a retry of everything in the queue
                            if (iInternalCritiMonResponseHandler != null) {
                                iInternalCritiMonResponseHandler.retryCrashAfterInitialisation();
                            }
                        }
                    }
                    catch (JSONException ex) {
                        Log.e("APIHandler", ex.toString());
                    }
                }

                //Check if undergoing maintenance returned if so, retry as the load balancer
                //should update pretty much straight away
                if (jsonObject.getInt("result") == 5 && iInternalCritiMonResponseHandler != null)
                {
                    iInternalCritiMonResponseHandler.retryInitialisation();
                }
                return jsonObject;
            }
            else
            {
                return null;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    protected void onPostExecute(JSONObject jsonObject)
    {
        if (jsonObject != null)
        {
            if (apiCall == APIHandler.API_Call.Initialise)
            {
                if (iCritiMonResultHandler != null)
                {
                    iCritiMonResultHandler.processResult(apiCall, jsonObject);
                }
            }
            else if (apiCall == APIHandler.API_Call.SendCrash)
            {
                if (iCritiMonResultHandler != null)
                {
                    iCritiMonResultHandler.processResult(apiCall, jsonObject);
                }

            }
        }
    }

    private void setSessionIDFromCookie(String cookie)
    {
        String[] cookieData = cookie.split(";");

        String nameAndValueString = "";
        if (cookieData[0].startsWith("SESSIONID"))
        {
            nameAndValueString = cookieData[0].trim();
        }
        else if ((cookieData.length > 1) && cookieData[1].startsWith("SESSIONID"))
        {
            nameAndValueString = cookieData[0].trim();
        }
        else
        {
            Log.d("CritiMon", "Session ID not found in cookie string");
        }

        if (!nameAndValueString.isEmpty())
        {
            String[] nameAndValue = nameAndValueString.split("=");
            if (nameAndValue.length == 2)
            {
                sessionID = nameAndValue[1];
            }
            else
            {
                Log.d("CritiMon", "SESSIONID looks to be blank");
            }
        }
    }

    private void setDOLBFromCookie(String cookie)
    {
        String[] cookieData = cookie.split(";");
        String nameAndValueString = "";
        if (cookieData[0].startsWith("DO-LB"))
        {
            nameAndValueString = cookieData[0].trim();
        }
        else if ((cookieData.length > 1) && cookieData[1].startsWith("DO-LB"))
        {
            nameAndValueString = cookieData[0].trim();
        }
        else
        {
            Log.d("CritiMon", "DO-LB not found in cookie string");
        }

        if (!nameAndValueString.isEmpty())
        {
            String[] nameAndValue = nameAndValueString.split("=");
            if (nameAndValue.length == 2)
            {
                DOLB = nameAndValue[1];
            }
            else
            {
                Log.d("CritiMon", "DO-LB looks to be blank");
            }
        }
    }
}
