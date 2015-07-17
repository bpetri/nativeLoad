package com.example.bjoern.nativeload;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by bjoern on 28.05.15.
 */

public class LogCatReader {

        private Process proc;
        private LogCatOut logcatOut;

        public LogCatReader(LogCatOut logcatOut)
        {
            this.logcatOut = logcatOut;
        }

        private InputStream inStd;

        private InputStream inErr;

        private LogcatProcessStreamReader streamReader;
        private LogcatProcessStreamReader errStreamReader;

        public void start()
        {
            try
            {
//                proc = Runtime.getRuntime().exec(new String[] {"logcat", " -d", " -v", " raw", " celix:V", " *:S" });

                proc = Runtime.getRuntime().exec(new String[] {"logcat", " -d", " -v", " raw", " celix:V", " *:S" });

                OutputStream os = proc.getOutputStream();

                this.inStd = proc.getInputStream();
                this.inErr = proc.getErrorStream();

                startReaders();

                os.flush();
            } catch (Exception e1)
            {
//            App.logExecption("Can't logcata", e1);
            }
        }

        private void startReaders() throws FileNotFoundException
        {
            this.streamReader = new LogcatProcessStreamReader(this.inStd, logcatOut);
            this.errStreamReader = new LogcatProcessStreamReader(this.inErr, null);

            streamReader.start();
            errStreamReader.start();
        }

        public void kill()
        {
            proc.destroy();
            if (this.streamReader != null)
                this.streamReader.finish();
            if (this.errStreamReader != null)
                this.errStreamReader.finish();
        }


        class LogcatProcessStreamReader extends Thread
        {


            private InputStream in;
            BufferedReader reader;
            private boolean done = false;
            private LogCatOut logcatOut;

            public LogcatProcessStreamReader(InputStream in, LogCatOut logcatOut)
            {
                this.in = in;
                this.reader = new BufferedReader(new InputStreamReader(in));
                this.logcatOut = logcatOut;
            }

            @Override
            public void run()
            {
                int read;

                try
                {
                    String line = "";

                    while (!done && (line = reader.readLine()) != null)
                    {
                            if (logcatOut != null)
                                logcatOut.writeLogData(line);
                    }
                    /*
                    while (!done && ((read = in. read(b)) != -1))
                    {
                        if(logcatOut != null)
                            logcatOut.writeLogData(b, read);
                    }
*/
                    if(logcatOut != null)
                        logcatOut.cleanUp();
                }
                /*
                byte[] b = new byte[8 * 1024];
                int read;

                try
                {
                    while (!done && ((read = in. read(b)) != -1))
                    {
                        if(logcatOut != null)
                            logcatOut.writeLogData(b, read);
                    }

                    if(logcatOut != null)
                        logcatOut.cleanUp();
                }
                */
                catch (IOException e)
                {
//                App.logExecption("Can't stream", e);
                }
            }

            public synchronized void finish()
            {
                done = true;
            }
        }
    }


