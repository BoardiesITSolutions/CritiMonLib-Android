package com.BoardiesITSolutions.CritiMonLib;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
//import org.apache.http.NameValuePair;
//import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Chris on 24/07/2017.
 */

public class CritiMon implements ICritiMonResultHandler
{
    protected static boolean CritiMonInitialised = false;
    protected static String SessionID ;
    protected static Context context;
    public enum CrashSeverity {Low, Medium, Major, Critical}
    protected static String APIKey;
    protected static String AppID;
    private static ICritiMonResultHandler iCritiMonResultHandler = null;
    protected static Thread.UncaughtExceptionHandler systemUnhandledExceptionHandler = null;

    private void InitialiseCritiMon(Context context, String apiKey, String appID)
    {
        //this.setAPICall(API_Call.Initialise);
        //this.setICritiMonResultHandler(this);
        setunhandledExceptionHandler();
        CritiMon.context = context;
        CritiMon.APIKey = apiKey;
        CritiMon.AppID = appID;
        //List<NameValuePair> postData = new ArrayList<>();
        HashMap<String, String> postData = new HashMap<>();
        postData.put("APIKey", apiKey);
        postData.put("ApplicationID", appID);
        postData.put("DeviceID", Helpers.getDeviceUID(context));
        APIHandler apiHandler = new APIHandler(APIHandler.API_Call.Initialise, this);
        apiHandler.execute(postData);
        //addPostData("APIKey", apiKey);
        //addPostData("ApplicationID", appID);
        //execute(getPostData());
    }

    private void setunhandledExceptionHandler()
    {
        final Thread.UncaughtExceptionHandler systemExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.UncaughtExceptionHandler myUnhandledExceptionHandler = new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException(Thread thread, Throwable ex)
            {
                Log.e("CritiMon", ((Exception) ex).toString(), ex);
                //CrashReporter.ReportUnhandledCrash(((Exception) ex));
                new CrashManager().ReportCrash((Exception) ex, CrashSeverity.Critical, CrashManager.CrashType.Unhandled);
                if (systemExceptionHandler != null)
                {
                    systemExceptionHandler.uncaughtException(thread, ex);
                }
            }
        };
        Thread.setDefaultUncaughtExceptionHandler(myUnhandledExceptionHandler);

    }

    public static void Initialise(Context context, String apiKey, String appID)
    {
        new CritiMon().InitialiseCritiMon(context, apiKey, appID);
    }

    protected static void Initialise(Context context, String apiKey, String appID, ICritiMonResultHandler resultHandler)
    {
        iCritiMonResultHandler = resultHandler;
        new CritiMon().InitialiseCritiMon(context, apiKey, appID);
    }

    public static void ReportCrash(Exception ex, CrashSeverity crashSeverity)
    {
        new CrashManager().ReportCrash(ex, crashSeverity);
    }

    public static void ReportCrash(Exception ex, CrashSeverity crashSeverity, String key, String value)
    {
        new CrashManager().ReportCrash(ex, crashSeverity, key, value);
    }

    public static void ReportCrash(Exception ex, CrashSeverity crashSeverity, JSONObject jsonObject)
    {
        new CrashManager().ReportCrash(ex, crashSeverity, jsonObject);
    }


    @Override
    public void processResult(APIHandler.API_Call api_call, JSONObject resultObj)
    {
        if (resultObj != null)
        {
            try
            {
                if (resultObj.getInt("result") == 0)
                {
                    CritiMon.CritiMonInitialised = true;

                    if (CritiMon.iCritiMonResultHandler != null)
                    {
                        CritiMon.iCritiMonResultHandler.processResult(api_call, resultObj);
                    }
                    CritiMon.iCritiMonResultHandler = null;
                }
                else
                {
                    Log.e("CritiMon", "Failed to initialise. Error: " + resultObj.getString("message"));
                    CritiMon.CritiMonInitialised = false;
                }
            }
            catch (JSONException e)
            {
                Log.e("CritiMon", "Error processing response: " + e.toString());
            }
        }
        else
        {
            Log.e("CritiMon", "CritiMon process result returned empty json object");
        }
    }
}
