/**
 * Licensed under Apache License v2. See LICENSE for more information.
 */
package com.inaetics.demonstrator.logging;

public abstract class LogCatOut
{
    //public abstract void writeLogData(byte[] data, int read) throws IOException;
    public abstract void writeLogData(String line);

    void cleanUp()
    {

    }
}
