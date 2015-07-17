package com.example.bjoern.nativeload;

import java.io.IOException;

/**
 * Created by bjoern on 28.05.15.
 */public abstract class LogCatOut
{
    //public abstract void writeLogData(byte[] data, int read) throws IOException;
    public abstract void writeLogData(String line) throws IOException;

    protected void cleanUp()
    {

    }
}
