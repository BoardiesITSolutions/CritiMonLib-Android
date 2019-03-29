package com.BoardiesITSolutions.CritiMonLib;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Random;

/**
 * Created by Chris on 29/01/2018.
 */

public class Helpers
{
    public static String getDeviceUID(Context context)
    {
        SharedPreferences settings = context.getSharedPreferences("critimon_preferences", 0);

        if (settings.contains("device_uid"))
        {
            return settings.getString("device_uid", "unknown");
        }

        //Generate the uid, store it and then return it
        String uid = generateRandomString();
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("device_uid", uid);
        editor.apply();

        return uid;
    }

    private static String generateRandomString()
    {
        String allowedChars = "abcdefghijklkmnopqrstuvwxyz0123456789";
        StringBuilder uidBuilder = new StringBuilder();
        Random rnd = new Random();
        int i = 0;
        while (i < 20)
        {
            int chosenChar = (int) (rnd.nextFloat() * allowedChars.length());
            uidBuilder.append(allowedChars.charAt(chosenChar));
            i++;
        }
        return uidBuilder.toString();
    }
}
