/**
 * Licensed under Apache License v2. See LICENSE for more information.
 */

package com.inaetics.demonstrator.logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class LogCatReader {
        private Process proc;
        private LogCatOut logcatOut;
        private InputStream inStd;
        private LogcatProcessStreamReader streamReader;

        public LogCatReader(LogCatOut logcatOut)
        {
            this.logcatOut = logcatOut;
        }


        public void start()
        {
            try
            {
                //Flush (Clear) logcat before starting to prevent big load time.
                Runtime.getRuntime().exec("logcat -c");
                // -v raw   --> No tags
                // printf:V --> Printf's from jni_part.c C logger
                // celix:V  --> __android_log_print
                // *:S      --> Ignore everything else
                proc = Runtime.getRuntime().exec(new String[] {"logcat", "-v", "raw", "printf:V", "celix:V", "*:S"});

                OutputStream os = proc.getOutputStream();
                this.inStd = proc.getInputStream();
                startReaders();
                os.flush();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

    private void startReaders()
        {
            this.streamReader = new LogcatProcessStreamReader(this.inStd, logcatOut);
            streamReader.start();
        }

        public void kill()
        {
            proc.destroy();
            if (this.streamReader != null)
                this.streamReader.finish();
        }


        private static class LogcatProcessStreamReader extends Thread
        {

            BufferedReader reader;
            private boolean done = false;
            private LogCatOut logcatOut;

            public LogcatProcessStreamReader(InputStream in, LogCatOut logcatOut)
            {
                this.reader = new BufferedReader(new InputStreamReader(in));
                this.logcatOut = logcatOut;
            }

            @Override
            public void run()
            {
                try
                {
                    String line;

                    while (!done && (line = reader.readLine()) != null)
                    {
                            if (logcatOut != null)
                                logcatOut.writeLogData(line);
                    }
                    if(logcatOut != null)
                        logcatOut.cleanUp();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public synchronized void finish()
            {
                done = true;
            }
        }
    }
