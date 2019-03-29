package com.BoardiesITSolutions.CritiMonLib;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
//import org.apache.http.HttpResponse;
//import org.apache.http.NameValuePair;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.ResponseHandler;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.conn.HttpHostConnectException;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.message.BasicNameValuePair;
//import org.apache.http.params.HttpConnectionParams;
//import org.apache.http.params.HttpParams;
//import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Chris on 25/10/2017.
 */

/*public class APIHandler_old extends AsyncTask<List<NameValuePair>, Void, JSONObject>
{
    protected static final int API_NOT_INITIALISED = 4;
    protected enum API_Call  {Initialise, SendCrash}
    private API_Call apiCall;

    private ICritiMonResultHandler iCritiMonResultHandler = null;
    private static HttpClient httpClient = null;

    public APIHandler_old(API_Call apiCall)
    {
        this.apiCall = apiCall;
    }

    public APIHandler_old(API_Call apiCall, ICritiMonResultHandler iCritiMonResultHandler)
    {
        this.apiCall = apiCall;
        this.iCritiMonResultHandler = iCritiMonResultHandler;
    }

    @Override
    protected JSONObject doInBackground(List<NameValuePair>... lists)
    {
        /*String responseBody = null;
        String serverURL = CritiMon.context.getString(R.string.critimon_url) + "/";
        //If the server URL is just a slash, it means the app hasn't tried to override (only done by Boardies IT Solutions)
        if (serverURL.equals("/"))
        {
            serverURL = "https://critimon.engine.boardiesitsolutions.com/";
        }
        Log.d("CritiMon", "Using URL: " + serverURL);
        ResponseHandler<String> responseHandler = null;
        List<NameValuePair> postData = lists[0];
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
        try
        {

            /*postData = new ArrayList<>();
            postData.add(new BasicNameValuePair("Test", "Item 1"));
            postData.add(new BasicNameValuePair("Test 2", "Item 2"));*/

            //Get the authorisation token from the list array
            /*String authorisationToken = "";
            for (int i = 0; i < postData.size(); i++)
            {
                if (postData.get(i).getName().equals("APIKey"))
                {
                    authorisationToken = postData.get(i).getValue();
                    postData.remove(i);
                }
            }

            HttpParams httpParams =

            if (httpClient == null)
            {
                //AndroidHttpClient client = new AndroidHttpClient();
                httpClient = new DefaultHttpClient();
            }
            HttpParams httpParams = httpClient.getParams();



            HttpConnectionParams.setConnectionTimeout(httpParams, 8000);
            HttpConnectionParams.setSoTimeout(httpParams, 8000);
            HttpPost httpPost = new HttpPost(serverURL);
            //httpPost.setHeader("User-Agent", "Android CritiMon Library");
            //httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setHeader("Authorisation-Token", authorisationToken);
            httpPost.setHeader("Connection", "close");
            //httpPost.setHeader("Cookie", "SESSIONID=zd8d5n3kucysl4idug1911m7ye");
            httpPost.setEntity(new UrlEncodedFormEntity(postData));

            String headers = "";

            responseHandler = new ResponseHandler<String>()
            {
                @Override
                public String handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException
                {
                    Log.d("CritiMon", "HandleResponse: " + ((httpResponse != null) ? httpResponse.toString() : "HTTP Response was Null"));
                    return EntityUtils.toString(httpResponse.getEntity());
                }
            };
            responseBody = httpClient.execute(httpPost, responseHandler);
            //httpClient.execute(httpPost);


            //responseBody = "{\"result\":0,\"message\":\"\",\"data\":{\"ApplicationName\":\"Test CritiMon App\",\"UserID\":\"3\"}}";

            Log.d("Response", responseBody);
            //httpClient = null;
            if (responseBody != null && !responseBody.isEmpty())
            {
                httpClient.getConnectionManager().closeIdleConnections(0, TimeUnit.NANOSECONDS);
                JSONObject jsonObject = new JSONObject(responseBody);
                return jsonObject;
            }
            else
            {
                httpClient.getConnectionManager().closeIdleConnections(0, TimeUnit.NANOSECONDS);
                return null;
            }
        }
        catch (HttpHostConnectException ex)
        {
            Log.e("CritiMon", "HttpPostConnectException: " + ex.toString());
            httpClient.getConnectionManager().closeIdleConnections(0, TimeUnit.NANOSECONDS);
            return null;
        }
        catch (UnknownHostException ex)
        {
            Log.e("CritiMon", "UnknownHostException: " + ex.toString());
            httpClient.getConnectionManager().closeIdleConnections(0, TimeUnit.NANOSECONDS);
            return null;
        }
        catch (JSONException ex)
        {
            Log.e("CritiMon", "JSONException: " + ex.toString());
            httpClient.getConnectionManager().closeIdleConnections(0, TimeUnit.NANOSECONDS);
            return null;
        }
        catch (SocketTimeoutException ex)
        {
            Log.e("CritiMon", "SocketTimeoutException: " + ex.toString());
            httpClient.getConnectionManager().closeIdleConnections(0, TimeUnit.NANOSECONDS);
            return null;
        }
        catch (SocketException ex)
        {
            Log.e("CritiMon", "SocketException: " + ex.toString());
            httpClient.getConnectionManager().closeIdleConnections(0, TimeUnit.NANOSECONDS);
            return null;
        }
        catch (ClientProtocolException ex)
        {
            Log.e("CritiMon", "ClientProtocolException: " + ex.toString());
            httpClient.getConnectionManager().closeIdleConnections(0, TimeUnit.NANOSECONDS);
            ex.printStackTrace();
            return null;
        }
        catch (UnsupportedEncodingException ex)
        {
            Log.e("CritiMon", "UnsupportedEncodingException: " + ex.toString());
            httpClient.getConnectionManager().closeIdleConnections(0, TimeUnit.NANOSECONDS);
            return null;
        }
        catch (IOException ex)
        {
            Log.e("CritiMon", "IOException: " + ex.toString());
            httpClient.getConnectionManager().closeIdleConnections(0, TimeUnit.NANOSECONDS);
            return null;
        }
        catch (Exception ex)
        {
            Log.e("CritiMon", "GeneralException: " + ex.toString());
            httpClient.getConnectionManager().closeIdleConnections(0, TimeUnit.NANOSECONDS);
            return null;
        }
            return null;
    }

    protected void onPostExecute(JSONObject jsonObject)
    {
        if (jsonObject != null)
        {
            if (apiCall == API_Call.Initialise)
            {
                if (iCritiMonResultHandler != null)
                {
                    //iCritiMonResultHandler.processResult(apiCall, jsonObject);
                }
            }
            else if (apiCall == API_Call.SendCrash)
            {
                if (iCritiMonResultHandler != null)
                {
                    //iCritiMonResultHandler.processResult(apiCall, jsonObject);
                }
            }
        }
    }
}*/
