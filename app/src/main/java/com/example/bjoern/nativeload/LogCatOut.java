/**
 * Licensed under Apache License v2. See LICENSE for more information.
 */
package com.example.bjoern.nativeload;

import java.io.IOException;

public abstract class LogCatOut
{
    //public abstract void writeLogData(byte[] data, int read) throws IOException;
    public abstract void writeLogData(String line) throws IOException;

    protected void cleanUp()
    {

    }
}
