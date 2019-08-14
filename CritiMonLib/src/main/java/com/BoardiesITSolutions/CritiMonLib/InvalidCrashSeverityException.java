package com.BoardiesITSolutions.CritiMonLib;

/**
 * Copyright (C) Chris Board - Boardies IT Solutions
 * August 2019
 * https://critimon.com
 * https://support.boardiesitsolutions.com
 */

public class InvalidCrashSeverityException extends Exception
{
    public InvalidCrashSeverityException()
    {
        super("Invalid crash severity provided. Use CritiMon.CrashSeverity enum");
    }
}
