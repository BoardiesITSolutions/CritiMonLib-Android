package com.BoardiesITSolutions.CritiMonLib;

import org.json.JSONObject;

/**
 * Created by Chris on 21/10/2017.
 */

public interface ICritiMonResultHandler
{
    void processResult(APIHandler.API_Call api_call, JSONObject resultObj);
}
