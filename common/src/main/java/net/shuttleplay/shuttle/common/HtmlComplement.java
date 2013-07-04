package net.shuttleplay.shuttle.common;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class HtmlComplement 
{
	public static interface Complement
	{
		public String getParameter(String name);
	}
	
	public static void process(Reader in, PrintWriter out, Complement comp)
	{
    	if (in != null && out != null)
    	{
    		StringWriter writer = new StringWriter();
    		try {
    		NetUtil.copyText(in, writer);
    		String buf = writer.getBuffer().toString();
    		
    		Pattern p = Pattern.compile("<%(\\w*)%>");
    		Matcher  m = p.matcher(buf);
    		int start = 0;

    		while (m.find())
    		{
    			MatchResult mr = m.toMatchResult();
    			String rep = "";
    			rep = comp.getParameter(mr.group(1));
    			
    			out.print(buf.substring(start, mr.start()));
    			out.print(rep);
    			start = mr.end();
    		} 
    		
    		out.print(buf.substring(start));
    		}
    		catch (IOException e)
    		{
    			Log.e("TrendBox", "Read input error", e);
    		}
    	}
	}
}
