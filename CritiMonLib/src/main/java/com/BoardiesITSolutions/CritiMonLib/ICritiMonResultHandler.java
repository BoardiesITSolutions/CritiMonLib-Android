package com.BoardiesITSolutions.CritiMonLib;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Copyright (C) Chris Board - Boardies IT Solutions
 * August 2019
 * https://critimon.com
 * https://support.boardiesitsolutions.com
 */

public interface ICritiMonResultHandler
{
    void processResult(APIHandler.API_Call api_call, JSONObject resultObj);

}

