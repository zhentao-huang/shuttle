package net.sf.webdav;

import android.util.Log;

public class Logger
{
    public static final String tag = "Webdav";

    public static Logger LOG = new Logger();

    public static Logger getInstance()
    {
        return LOG;
    }

    public void trace(String str)
    {
        Log.i(tag, str);
    }

    public void warn(String str)
    {
        Log.w(tag, str);
    }

    public void error(String str)
    {
        Log.e(tag, str);
    }

    public void error(String str, Exception exp)
    {
        Log.e(tag, str, exp);
    }

    public boolean isTraceEnabled()
    {
        return true;
    }
}
