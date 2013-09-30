package net.shuttleplay.shuttle.appshare;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.eclipse.jetty.util.IO;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.MimeTypeMap;

//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.WriterException;
//import com.google.zxing.MultiFormatWriter;
//import com.google.zxing.common.BitMatrix;
import net.shuttleplay.shuttle.common.NetUtil;
import net.shuttleplay.shuttle.common.TagWriter;

public class ModuleListServlet extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2686052244426469843L;
	
	private static final String TAG = "Shuttle";


	public ModuleListServlet()
	{
		super();
		mShuttlePackages = new Hashtable<String, ModuleListServlet.PackageData>();
        mOrders = new ArrayList<String>();
		Log.i(TAG, "ModuleListServlet initialized");
		mInited = false;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (!mInited)
		{
            mPath = "/appshare/modlist/";
            mInited = true;
		}
		String pathInfo = req.getPathInfo();
		
		if (pathInfo == null || pathInfo.equals("/"))
		{
			synchronized (mShuttlePackages)
			{
				if (mShuttlePackages.isEmpty())
				{
					updateShuttlePackages();
				}
				resp.setContentType("text/plain;charset=UTF-8");
				resp.getWriter().print(mShuttlePackages.size());
			}
		}
		else if (pathInfo.startsWith("/icon"))
		{
//			synchronized (mShuttlePackages)
			{
				String packageName = pathInfo.substring("/icon/".length());
				PackageData data = mShuttlePackages.get(packageName);
				OutputStream out =resp.getOutputStream();
				if (data != null && data.mInputStream == null)
				{
					resp.setContentType("image/png");
					if (data.mIcon instanceof BitmapDrawable)
					{
						BitmapDrawable bd = (BitmapDrawable) data.mIcon;
						ByteArrayOutputStream bytes = new ByteArrayOutputStream();
						
						Bitmap bitmap = bd.getBitmap();
						bitmap.compress(Bitmap.CompressFormat.PNG,100,bytes);
		                data.mInputStream = new ByteArrayInputStream(bytes.toByteArray());
					}
				}
		
				if (data != null && data.mInputStream != null)
				{
					Log.i(TAG, "doGet write to stream");
					data.mInputStream.reset();
					NetUtil.copyIO(data.mInputStream,out);
				}
				else
				{
					Log.e(TAG, "doGet Can't get data.mInputStream");
				}
			}
		}
		else if (pathInfo.startsWith("/start"))
		{
			String packageName = pathInfo.substring("/start/".length());
			PackageData data = mShuttlePackages.get(packageName);
			if (data.mStartIntent != null)
			{
				getAndroidContext().startActivity(data.mStartIntent);
			}
		}
		/*
		else if (pathInfo.startsWith("/ip"))
		{
			resp.setContentType("text/plain");
			PrintWriter out = resp.getWriter();
			out.print("http://" + getLocalIpAddress() + ":8000/appshare");
		}
		else if (pathInfo.startsWith("/ip2"))
		{
			resp.setContentType("text/plain");
			PrintWriter out = resp.getWriter();
			out.print("http://" + getLocalIpAddress() + ":8000/appshare/index2.html");
		}
		*/
		else if (pathInfo.startsWith("/down"))
		{
			String packageName = pathInfo.substring("/down/".length());
			PackageData data = mShuttlePackages.get(packageName);
			Log.i(TAG, "Begin download");
			if (data.mSourceApk != null)
			{
				File file = new File(data.mSourceApk);
				Log.i(TAG, "Find source APK " + data.mSourceApk);
				
				if (file.exists())
				{
					Log.i(TAG, "File " + data.mSourceApk + " exists");
					String contentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("apk");
					resp.setContentType(contentType);
					resp.setContentLength((int) file.length());
					resp.addHeader("Content-Disposition", "attachment;filename=" + file.getName());
					Log.i(TAG, "Set content length " + file.length());
					Log.i(TAG, "Set contentType " + contentType);
					Log.i(TAG, "Set filename " + file.getName());
					InputStream in = new BufferedInputStream(new FileInputStream(file));
					OutputStream out = resp.getOutputStream();
					NetUtil.copyIO(in, out);
                    synchronized (getAndroidContext().getMainLooper())
                    {
                        Context context = getAndroidContext();
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                        int c = sp.getInt("SharedCounter", 0);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putInt("SharedCounter", ++c);
                        editor.commit();
                    }
				}
			}
			Log.i(TAG, "Download end");
		}
		else if (pathInfo.startsWith("/get"))
		{
			
		}
		else if (pathInfo.startsWith("/view"))
		{
			synchronized (mShuttlePackages)
			{
				if (mShuttlePackages.isEmpty())
				{
					updateShuttlePackages();
				}
				
				resp.setContentType("text/html;charset=UTF-8");
				TagWriter table = new TagWriter(resp.getWriter(), "table");
				table.setIndent(2);
                String qrcode = getQrCodeString();
                for (String pname : mOrders)
//                        PackageData data : new ArrayList<PackageData>(mShuttlePackages.values()))
				{
                    PackageData data = mShuttlePackages.get(pname);
					table
					.addChild("tr").push()
						.addChild("td");
//							.addChild("a").addAttr("href", mPath + "start/" +data.mPackageName)
//								.addChild("img").addAttr("src", mPath + "icon/" + data.mPackageName).end()
//								.end()
					
					boolean readable = isSourceReadable(data.mSourceApk);
					if (readable)
					{
						table.last().addChild("a").addAttr("href", mPath + "down/" +data.mPackageName);
					}
					table.last().addChild("img").addAttr("src", mPath + "icon/" + data.mPackageName)
							.addAttr("height", "72").addAttr("width", "72").end();
							
					table.pop()
						.addChild("td")
							.addText(data.mLabel).end()
						.addChild("td");
					
					Log.i(TAG, "Write a label " + data.mLabel);

//					String hosturl = req.getHeader("REFERER");
//					
//					Log.i(TAG, "Get host address " + getLocalIpAddress());
//					Log.i(TAG, "Get referer " + hosturl);
//					Log.i(TAG, "Get request url " + req.getRequestURI() + "|||" + req.getRequestURL());
//					Log.i(TAG, "After encoded " + URLEncoder.encode(hosturl, "UTF-8"));
					
					if (readable)
					{
						String localip = NetUtil.getLocalIpAddress(getAndroidContext());

						String localapk = "http://" + localip + ":8000/appshare/modlist" + (mNonShuttle ?"2":"") + "/down/" + data.mPackageName;
						
						StringBuilder urlstr = new StringBuilder();
	//					urlstr.append("https://chart.googleapis.com/chart?");
						urlstr.append("http://" + localip + ":8000/manager/qr/");
						urlstr.append(localapk);
						
						Log.i(TAG, "URL = \n" + urlstr.toString());
						
						table.last()
								.addChild("a").addAttr("href", urlstr.toString())
									.addText(qrcode)
									.end()
								.end()
							.end();
					}
					else
					{
						table.last().addText("Locked");
						Log.d(TAG, "Get locked source " + data.mSourceApk);
					}
				}
				
				table.finish();
			}
		}
		/*
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
				copyIO(in,out);
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
		*/
//		else if (pathInfo.startsWith("/description"))
//		{
//			synchronized (mShuttlePackages)
//			{
//				if (mShuttlePackages.isEmpty())
//				{
//					updateShuttlePackages();
//				}
//				
//				String name = pathInfo.substring("/description/".length());
//				PackageData data = mShuttlePackages.get(name);
//				ClassLoader loader = data.mContext.getClassLoader();
//				try {
//					Class<?> clazz = loader.loadClass(data.mPackageName+".R$string");
//					Field field = clazz.getField("description");
//					int id = field.getInt(null);
//					data.mDescription = data.mResources.getString(id);
//				} catch (ClassNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (SecurityException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (NoSuchFieldException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (IllegalArgumentException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (IllegalAccessException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
	}
	
	private static final String ANDROID_CONTEXT_NAME = "net.shuttleplay.shuttle.context";
	
	private boolean isSourceReadable(String src)
	{
		if (src != null)
		{
			File file = new File(src);
			return file.canRead();
		}
		return false;
	}
	
	private void updateShuttlePackages()
	{
		if (mPackageManager == null)
		{
			Context context = getAndroidContext();
			mPackageManager = context.getPackageManager();
		}

		List<PackageInfo> packages = mPackageManager.getInstalledPackages(0);
		mShuttlePackages.clear();
		
		
		for (PackageInfo pinfo : packages)
		{
			Context context = getAndroidContext();
			
			if (pinfo.packageName != null &&
                pinfo.applicationInfo != null &&
                pinfo.applicationInfo.sourceDir != null &&
                !pinfo.applicationInfo.sourceDir.startsWith("/system/app/"))
			{
				try {

					PackageData data = new PackageData();
					data.mPackageName = pinfo.packageName;
					data.mIcon = mPackageManager.getApplicationIcon(data.mPackageName);
					data.mLabel = mPackageManager.getApplicationLabel(
							mPackageManager.getApplicationInfo(data.mPackageName, PackageManager.GET_META_DATA)).toString();
					data.mStartIntent = mPackageManager.getLaunchIntentForPackage(data.mPackageName);
					if (data.mStartIntent != null)
					{
						data.mStartIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					}
					mShuttlePackages.put(data.mPackageName, data);

                    if (data.mPackageName.equals(context.getPackageName()))
                    {
                        mSelf = data;
                    }
                    else if (data.mPackageName.startsWith("net.shuttleplay."))
                    {
                        mOrders.add(0, data.mPackageName);
                    }
                    else
                    {
                        mOrders.add(data.mPackageName);
                    }
					Log.i(TAG, "Get a drawable instance : " + data.mIcon.getClass().getName());
					
//					data.mContext = context.createPackageContext(data.mPackageName, Context.CONTEXT_INCLUDE_CODE);
//					data.mResources = data.mContext.getResources();
					data.mSourceApk = pinfo.applicationInfo.sourceDir;
				} catch (NameNotFoundException e) {
				}
			}
		} // End for
        mOrders.add(0, mSelf.mPackageName);
	}
	
	private Context getAndroidContext()
	{
		ServletContext sContext = getServletContext();
		Context context = (Context) sContext.getAttribute(ANDROID_CONTEXT_NAME);
		return context;
	}

    private String getQrCodeString()
    {
        Context context = getAndroidContext();
        Resources res = context.getResources();
        int id = res.getIdentifier("qrcode","string", context.getPackageName());
        return res.getString(id);
    }
	
	private class PackageData
	{
		public String mLabel;
		public String mPackageName;
		public Drawable mIcon;
		public ByteArrayInputStream mInputStream;
		public Intent mStartIntent;
//		public Context mContext;
//		public Resources mResources;
//		public String mDescription;
		public String mSourceApk;
	}
	

	
	PackageManager mPackageManager;
    ArrayList<String> mOrders;
	Hashtable<String, PackageData> mShuttlePackages;
    PackageData mSelf;
	boolean mNonShuttle;
	String mPath;
	boolean mInited;
}
