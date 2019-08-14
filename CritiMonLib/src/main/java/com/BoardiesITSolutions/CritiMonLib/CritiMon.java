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
 * Copyright (C) Chris Board - Boardies IT Solutions
 * August 2019
 * https://critimon.com
 * https://support.boardiesitsolutions.com
 */

public class CritiMon implements ICritiMonResultHandler, IInternalCritiMonResponseHandler
{
    protected static boolean CritiMonInitialised = false;
    protected static String SessionID ;
    protected static Context context;

    @Override
    public void retryCrashAfterInitialisation()
    {
        //Not needed here
    }

    @Override
    public void retryInitialisation()
    {
        CritiMon.Initialise(CritiMon.context, CritiMon.APIKey, CritiMon.AppID, CritiMon.AppVersion);
    }

    public enum CrashSeverity {Low, Medium, Major, Critical}
    protected static String APIKey;
    protected static String AppID;
    protected static String AppVersion;
    private static ICritiMonResultHandler iCritiMonResultHandler = null;
    protected static Thread.UncaughtExceptionHandler systemUnhandledExceptionHandler = null;
    public static ArrayList<HashMap<String, String>> retryCrashInfoQueue = new ArrayList<>();


    private void InitialiseCritiMon(Context context, String apiKey, String appID, String appVersion)
    {
        //this.setAPICall(API_Call.Initialise);
        //this.setICritiMonResultHandler(this);
        setunhandledExceptionHandler();
        CritiMon.context = context;
        CritiMon.APIKey = apiKey;
        CritiMon.AppID = appID;
        CritiMon.AppVersion = appVersion;
        //List<NameValuePair> postData = new ArrayList<>();
        HashMap<String, String> postData = new HashMap<>();
        postData.put("APIKey", apiKey);
        postData.put("ApplicationID", appID);
        postData.put("DeviceID", Helpers.getDeviceUID(context));
        postData.put("AppVersion", appVersion);
        APIHandler apiHandler = new APIHandler(APIHandler.API_Call.Initialise, this);
        apiHandler.execute(postData);
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

    public static void Initialise(Context context, String apiKey, String appID, String appVersion)
    {
        new CritiMon().InitialiseCritiMon(context, apiKey, appID, appVersion);
    }

    protected static void Initialise(Context context, String apiKey, String appID, String appVersion, ICritiMonResultHandler resultHandler)
    {
        iCritiMonResultHandler = resultHandler;
        new CritiMon().InitialiseCritiMon(context, apiKey, appID, appVersion);
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
