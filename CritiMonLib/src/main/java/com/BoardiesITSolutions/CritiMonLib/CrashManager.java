package com.BoardiesITSolutions.CritiMonLib;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.renderscript.BaseObj;
import android.telephony.TelephonyManager;
import android.util.Log;
//import org.apache.http.NameValuePair;
//import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static java.security.AccessController.getContext;

/**
 * Created by Chris on 22/10/2017.
 */

class CrashManager implements ICritiMonResultHandler
{

    private HashMap<String, String> postData;
    private int initialiseRetryCount = 0;
    protected enum CrashType {Handled, Unhandled}

    protected CrashManager()
    {
        postData = new HashMap<>();
    }



    protected void ReportCrash(Exception ex, CritiMon.CrashSeverity crashSeverity)
    {
        ReportCrash(ex, crashSeverity, CrashType.Handled);
    }

    protected void ReportCrash(Exception ex, CritiMon.CrashSeverity crashSeverity, CrashType crashType)
    {
        try
        {
            addCrashSeverityToPostData(crashSeverity);
            addDeviceDataToPostFields();
            if (crashType == CrashType.Handled)
            {
                postData.put("CrashType", "Handled");
            }
            else
            {
                postData.put("CrashType", "Unhandled");
            }
            postData.put("ExceptionType", ex.getClass().getName());
            postData.put("ExceptionMessage", ex.toString());

            StringWriter writer = new StringWriter();
            ex.printStackTrace(new PrintWriter(writer));
            postData.put("Stacktrace", writer.toString());
            sendCrashData();
        }
        catch (InvalidCrashSeverityException e)
        {
            Log.e("CritiMon", e.toString());
        }
    }

    protected void ReportCrash(Exception ex, CritiMon.CrashSeverity crashSeverity, String key, String value)
    {
        try
        {
            /*addCrashSeverityToPostData(crashSeverity);
            addDeviceDataToPostFields();
            postData.add(new BasicNameValuePair("CrashType", "Handled"));
            postData.add(new BasicNameValuePair("ExceptionType", ex.getClass().getName()));
            StringWriter writer = new StringWriter();
            ex.printStackTrace(new PrintWriter(writer));
            postData.add(new BasicNameValuePair("Stacktrace", writer.toString()));
            postData.add(new BasicNameValuePair("PropertyKey", key));
            postData.add(new BasicNameValuePair("PropertyValue", value));
            sendCrashData();*/
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(key, value);
            this.ReportCrash(ex, crashSeverity, jsonObject);
        }
        catch (JSONException ex1)
        {

        }
    }

    protected void ReportCrash(Exception ex, CritiMon.CrashSeverity crashSeverity, String key, int value)
    {
        try
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(key, value);
            this.ReportCrash(ex, crashSeverity, jsonObject);
        }
        catch (JSONException ex1)
        {

        }
    }

    protected void ReportCrash(Exception ex, CritiMon.CrashSeverity crashSeverity, JSONObject jsonObject)
    {
        try
        {
            addCrashSeverityToPostData(crashSeverity);
            addDeviceDataToPostFields();
            postData.put("CrashType", "Handled");
            postData.put("ExceptionType", ex.getClass().getName());
            postData.put("ExceptionMessage", ex.toString());
            StringWriter writer = new StringWriter();
            ex.printStackTrace(new PrintWriter(writer));
            postData.put("Stacktrace", writer.toString());
            postData.put("CustomProperty", jsonObject.toString());
            sendCrashData();
        }
        catch (InvalidCrashSeverityException e)
        {
            Log.e("CritiMon", e.toString());
        }
    }

    private void addDeviceDataToPostFields()
    {
        if (CritiMon.CritiMonInitialised)
        {
            try
            {
                //Get the Device ID
                postData.put("DeviceID", Helpers.getDeviceUID(CritiMon.context));

                PackageInfo pInfo = CritiMon.context.getPackageManager().getPackageInfo(CritiMon.context.getPackageName(), 0);
                postData.put("VersionName", pInfo.versionName);
                postData.put("DeviceType", "Android");
                postData.put("ROMBuild", android.os.Build.DISPLAY);
                postData.put("KernelVersion", System.getProperty("os.version"));
                postData.put("DeviceBrand", android.os.Build.BRAND);
                postData.put("DeviceModel", android.os.Build.MODEL);
                postData.put("APILevel", String.valueOf(android.os.Build.VERSION.SDK_INT));
                postData.put("ScreenResolution", CritiMon.context.getResources().getDisplayMetrics().widthPixels + " x " + CritiMon.context.getResources().getDisplayMetrics().heightPixels);
                postData.put("Locale", Locale.getDefault().getDisplayLanguage().toString());
                TelephonyManager tMgr = (TelephonyManager) CritiMon.context.getSystemService(CritiMon.context.TELEPHONY_SERVICE);
                String mobileOperator = tMgr.getNetworkOperatorName();
                if (mobileOperator == null || mobileOperator.equals(""))
                {
                    postData.put("MobileNetwork", "N/A");
                }
                else
                {
                    postData.put("MobileNetwork", mobileOperator);
                }
            }
            catch (PackageManager.NameNotFoundException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            Log.e("CritiMon", "CritiMon not initialised. Call CritiMon.Initialise(context, api_key, app_id) before sending a crash");
        }
    }

    private void addCrashSeverityToPostData(CritiMon.CrashSeverity severity) throws InvalidCrashSeverityException
    {
        switch (severity)
        {
            case Low:
                postData.put("Severity", "Low");
                break;
            case Medium:
                postData.put("Severity", "Medium");
                break;
            case Major:
                postData.put("Severity", "Major");
                break;
            case Critical:
                postData.put("Severity", "Critical");
                break;
            default:
                throw new InvalidCrashSeverityException();
        }
    }

    private void sendCrashData()
    {
        try
        {

            if (CritiMon.CritiMonInitialised)
            {
                postData.put("APIKey", CritiMon.APIKey);
                postData.put("AppID", CritiMon.AppID);
                APIHandler apiHandler = new APIHandler(APIHandler.API_Call.SendCrash, this);
                apiHandler.execute(postData);
            }
            else
            {
                Log.e("CritiMon", "CritiMon not initialised. Call CritiMon.Initialise(context, api_key, app_id) before sending a crash");
            }
        }
        catch (NullPointerException ex)
        {
            Log.e("CritiMon-CrashManager", ex.toString());
        }
    }

    @Override
    public void processResult(APIHandler.API_Call api_call, JSONObject resultObj)
    {
        if (resultObj != null)
        {
            Log.d("CritiMon", resultObj.toString());

            try
            {
                if (resultObj.getInt("result") == APIHandler.API_NOT_INITIALISED)
                {
                    //Check was the library initialised already, if so, then probably nothing happened with this user
                    //for a while so engine removed session. Reinitialise and create a new session
                    if (CritiMon.CritiMonInitialised)
                    {
                        //Only attempt to reinitalise 3 times, make sure we don't get stuck in a loop.
                        if (initialiseRetryCount < 3)
                        {
                            initialiseRetryCount++;
                            CritiMon.Initialise(CritiMon.context, CritiMon.APIKey, CritiMon.AppID, new ICritiMonResultHandler()
                            {
                                @Override
                                public void processResult(APIHandler.API_Call api_call, JSONObject resultObj)
                                {
                                    //Now resend the crash
                                    sendCrashData();
                                }
                            });
                        }
                    }
                }
                else
                {
                    Log.e("CritiMon", resultObj.getString("message"));
                }
            }
            catch (JSONException e)
            {
                Log.e("CritiMon", e.toString());
            }
        }
        else
        {
            Log.d("CritiMon", "No json response");
        }
    }
}
