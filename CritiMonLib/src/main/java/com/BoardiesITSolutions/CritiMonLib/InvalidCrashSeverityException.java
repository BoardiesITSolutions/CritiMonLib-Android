package com.BoardiesITSolutions.CritiMonLib;

/**
 * Created by Chris on 22/10/2017.
 */

public class InvalidCrashSeverityException extends Exception
{
    public InvalidCrashSeverityException()
    {
        super("Invalid crash severity provided. Use CritiMon.CrashSeverity enum");
    }
}
