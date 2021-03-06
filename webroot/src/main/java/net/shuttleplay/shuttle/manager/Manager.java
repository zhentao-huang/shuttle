package net.shuttleplay.shuttle.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.AbstractDocument.Content;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.Handler;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import net.shuttleplay.shuttle.common.JettyService;
import net.shuttleplay.shuttle.common.NetUtil;
import net.shuttleplay.shuttle.common.TagWriter;
import net.shuttleplay.shuttle.common.URLEncoder;

public class Manager extends HttpServlet {
	
    private static final String ANDROID_CONTEXT_ATTRIBUTE = "net.shuttleplay.shuttle.context";
    
	private static final String TAG = "Shuttle";
	
    private Server mServer;
    private ContextHandler[] mHandlers;
    private Context mContext;

	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		super.init();
		
		final Context context = (Context) getServletContext().getAttribute(ANDROID_CONTEXT_ATTRIBUTE);
		context.bindService(new Intent("net.shuttleplay.shuttle.app.ShuttleService"), new ServiceConnection() {
			
			public void onServiceDisconnected(ComponentName arg0) {
				mServer = null;
				mHandlers = null;
			}
			
			public void onServiceConnected(ComponentName arg0, IBinder arg1) {
				JettyService js  = (JettyService) arg1;
				mServer = js.getServer();
				Handler[] handlers = mServer.getChildHandlersByClass(ContextHandler.class);
				
				mHandlers = new ContextHandler[handlers.length];
				
				int i = 0;
				for (Handler handler : handlers)
				{
					mHandlers[i++] = (ContextHandler) handler;
				}
				context.unbindService(this);
			}
		}, 0);
		//mServer = (Server) getServletContext().getAttribute(JETTY_SERVER_ATTRIBUTE);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		if (mServer == null)
		{
			return;
		}
		
		String pathInfo = req.getPathInfo();
		URLEncoder encoder = new URLEncoder();
		
		if (pathInfo == null || pathInfo.equals("/"))
		{
			TagWriter html = new TagWriter(resp.getWriter(), "html");
			resp.setContentType("text/html;charset=UTF-8");
			html.setIndent(2);
			html.addChild("head").addChild("title").addText("Manager Page")
								 .end()
		         .end()
		         .addChild("body").addChild("b").addText("Hello manager").finish();
		}
		else if (pathInfo.startsWith("/count"))
		{
			resp.setContentType("text/plain;charset=UTF-8");
			resp.getWriter().print(mHandlers.length - 1);
		}
		else if (pathInfo.startsWith("/view"))
		{
			resp.setContentType("text/html;charset=UTF-8");
			TagWriter table = new TagWriter(resp.getWriter(),"table");
			table.setIndent(2);
            String qrcode = getQrCodeString();
			for (ContextHandler handler : mHandlers)
			{
				String name = handler.getDisplayName();
				String contextPath = handler.getContextPath();
				StringBuilder builder = new StringBuilder();
				String img = null;
				builder.append("http://" + NetUtil.getLocalIpAddress(mContext) + ":8000");
				String base = builder.toString();
				builder.append(contextPath);
				if (contextPath.equals("/"))
				{
					continue;
				}
				else if (contextPath.equals("/webdav"))
				{
					img = builder.toString() + "/Shuttle/webapps/webdav/icon.png";
				}
				else
				{
					img = builder.toString() + "/icon.png";
				}
				String qr = base + "/manager/qr/" + builder.toString();
				table
				.addChild("tr")
					.addChild("td")
						.addChild("a").addAttr("href", builder.toString())
							.addChild("img").addAttr("src", img).addAttr("height", "72").addAttr("width", "72").end()
							.end()
						.end()
					.addChild("td")
						.addText(name)
						.end()
					.addChild("td")
						.addChild("a").addAttr("href", qr)
							.addText(qrcode);
			}
			table.finish();
		}
		else if (pathInfo.startsWith("/ip"))
		{
			resp.setContentType("text/plain;charset=UTF-8");
			PrintWriter out = resp.getWriter();
			String url = "http://" + NetUtil.getLocalIpAddress(mContext) + ":8000" + encoder.encode(pathInfo.substring("/ip".length())); 
			out.print(url);
		}
		else if (pathInfo.startsWith("/qrip"))
		{
			resp.setContentType("text/plain;charset=UTF-8");
			PrintWriter out = resp.getWriter();
			String url = "http://" + NetUtil.getLocalIpAddress(mContext) + ":8000" + encoder.encode(encoder.encode(pathInfo.substring("/qrip".length()))); 
			out.print("http://" + NetUtil.getLocalIpAddress(mContext) + ":8000/manager/qr/" + url);
		}
		else if (pathInfo.startsWith("/qr"))
		{
			String qrcode = pathInfo.substring("/qr/".length());
			BitMatrix byteMatrix;
			final int black = 0x0;
			final int white = 0xffffffff;
			try {
				byteMatrix = new MultiFormatWriter().encode(qrcode,BarcodeFormat.QR_CODE, 320, 320);
				Bitmap bitmap = Bitmap.createBitmap(320, 320, Bitmap.Config.RGB_565);
				for (int x = 0; x < 320; ++x)
				{
					for (int y = 0; y < 320 ; ++y)
					{
						bitmap.setPixel(x, y, byteMatrix.get(x,y) ? black : white);
					}
				}
				
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				
				bitmap.compress(Bitmap.CompressFormat.PNG,100,bytes);
				ByteArrayInputStream in = new ByteArrayInputStream(bytes.toByteArray());
				OutputStream out = resp.getOutputStream();
				resp.setContentType("image/png");
				NetUtil.copyIO(in,out);
			}
			catch (IOException e)
			{
				Log.e(TAG, "Generate qr code error", e);
			}
			catch (WriterException e)
			{
				Log.e(TAG, "Generate qr code error", e);
			}
			
		}
	}

    private String getQrCodeString()
    {
        Context context = (Context) getServletContext().getAttribute(ANDROID_CONTEXT_ATTRIBUTE);
        Resources res = context.getResources();
        int id = res.getIdentifier("qrcode","string", context.getPackageName());
        return res.getString(id);
    }

}
