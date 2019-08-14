package com.BoardiesITSolutions.CritiMonLib;

import java.util.HashMap;

/**
 * Copyright (C) Chris Board - Boardies IT Solutions
 * August 2019
 * https://critimon.com
 * https://support.boardiesitsolutions.com
 */

public interface IInternalCritiMonResponseHandler
{
    void retryCrashAfterInitialisation();
    void retryInitialisation();
}
