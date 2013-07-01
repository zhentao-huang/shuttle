package com.trendmicro.mobilelab.game.cchess;

import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import android.util.Log;
import android.content.Context;

import com.trendmicro.mobilelab.common.HtmlComplement;
import com.trendmicro.mobilelab.common.NetUtil;
import com.trendmicro.mobilelab.common.URLEncoder;

public class CChessServlet extends HttpServlet {
	
	@Override
	public void init() throws ServletException {
		super.init();
		mContext = getServletContext();
	}

	private static final String TAG = "TrendBox";
	private ServletContext mContext;

	private static final String ANDROID_CONTEXT_NAME = "com.trendmicro.mobilelab.toolbox.context";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		Log.i(TAG, "PathInfo = " + pathInfo);
		if (pathInfo == null || pathInfo.equals("/"))
		{
	    	InputStream in = mContext.getResourceAsStream("/game.html");
	    	if (in != null)
	    	{
	    		Log.i(TAG, "Find index.html");
	    		InputStreamReader reader = new InputStreamReader(in);
        		PrintWriter out = resp.getWriter();
                        Context context = (Context) mContext.getAttribute(ANDROID_CONTEXT_NAME);
				final String localip = NetUtil.getLocalIpAddress(context);
				final URLEncoder encoder = new URLEncoder();
        		HtmlComplement.process(reader, out, new HtmlComplement.Complement() {
					
					public String getParameter(String name) {
						Log.i(TAG, "Update parameter : " + name);
						if (name.equals("qrip"))
						{
							String url = getUrl();
							String ret = "http://" + localip + ":8000/manager/qr/" + encoder.encode(url);
							return ret;
						}
						else if (name.equals("url"))
						{
							return getUrl();
						}
						else if (name.equals("red") || name.equals("black"))
						{
							String url = getUrl();
							url += "side/" + name;
							return url;
						}
						else if (name.equals("redqr") || name.equals("blackqr"))
						{
							String url = getUrl();
							String n = name.substring(0, name.length()-2);
							url += "side/" + n;
							
							String ret = "http://" + localip + ":8000/manager/qr/" + encoder.encode(url);
							return ret;
						}
						return "";
					}
					
					private String getUrl()
					{
						if (mUrl == null)
						{
							mUrl = "http://" + localip + ":8000/cchess/game/";
						}
						return mUrl;
					}
					
					private String mUrl;
				});
        		
        		reader.close();
	    		resp.setContentType("text/html;charset=UTF-8");
	    	}
		}
		else if (pathInfo.startsWith("/side/"))
		{
			final String side = pathInfo.substring("/side/".length());
			if (side.equals("red") || side.equals("black"))
			{
				InputStream in = mContext.getResourceAsStream("/cchess.html");
				if (in != null)
				{
		    		InputStreamReader reader = new InputStreamReader(in);
	        		PrintWriter out = resp.getWriter();
	        		
	        		HtmlComplement.process(reader, out, new HtmlComplement.Complement() {
						
						public String getParameter(String name) {
							if (name.equals("side"))
							{
								return side;
							}
							return "";
						}
					});
	        		reader.close();
	        		resp.setContentType("text/html;charset=UTF-8");
				}
			}
		}
	}

}
