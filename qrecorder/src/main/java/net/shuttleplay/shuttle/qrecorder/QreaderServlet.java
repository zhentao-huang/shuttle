package net.shuttleplay.shuttle.qrecorder;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
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
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.webkit.MimeTypeMap;

//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.WriterException;
//import com.google.zxing.MultiFormatWriter;
//import com.google.zxing.common.BitMatrix;
import net.shuttleplay.shuttle.common.NetUtil;
import net.shuttleplay.shuttle.common.TagWriter;
import net.shuttleplay.shuttle.common.QrHistory;

public class QreaderServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 2686052244426469843L;

    private static final String TAG = "Shuttle";


    public QreaderServlet()
    {
        super();
        Log.i(TAG, "Qreader initialized");
        mInited = false;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!mInited)
        {
            mPath = "/qrecorder";
            mInited = true;
            mQrs = new QrHistory(getAndroidContext());
        }
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/qrnum"))
        {
            int count = mQrs.getCount();
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getWriter().print(count);
        }
//        else if (pathInfo.startsWith("/qrpick"))
//        {
//            if (data.mStartIntent != null)
//            {
//                Handler handler = new Handler(getAndroidContext().getMainLooper()) {
//                    public void handleMessage(Message msg)
//                    {
//
//                    }
//                }
//                Intent intent = new Intent(Intent.ACTION_PICK, Intent.CATEGORY_DEFAULT);
//                intent.putExtra("pickhandler", handler);
//                getAndroidContext().startActivity(intent);
//            }
//        }
        else if (pathInfo.startsWith("/text/"))
        {
            resp.setContentType("text/plain;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.print(pathInfo.substring("/text/".length()));
        }
        else if (pathInfo.startsWith("/qrlist"))
        {
            resp.setContentType("text/html;charset=UTF-8");

            Cursor cursor = mQrs.query();
            if (cursor.moveToFirst())
            {
                Log.i(TAG, "Prepare qrlist data");
                TagWriter table = new TagWriter(resp.getWriter(), "table");
                table.setIndent(2);
                int textIndex = cursor.getColumnIndex("rawtext");
                do
                {

                    table
                            .addChild("tr")
                            .addChild("td").push();

                    String text = cursor.getString(textIndex);
                    URL url = null;
                    try
                    {
                        url = new URL(text);
                    }
                    catch (MalformedURLException e)
                    {

                    }

                    Log.i(TAG, "In doGet url = " + url);
                    Log.i(TAG, "Get column count = " + cursor.getColumnCount());
                    for (int c=0; c<cursor.getColumnCount(); ++c)
                    {
                        Log.i(TAG, "" + c + ". " + cursor.getColumnName(c));
                    }

                    if (url == null)
                    {
                        table.last().addChild("a").addAttr("href", mPath +  "text/" + text)
                            .addText(shortString(text)).end();
                    }
                    else
                    {
                        table.last().addChild("a").addAttr("href", text).addAttr("target", "_blank")
                            .addText(shortString(text)).end();
                    }

                    table.pop()
                            .addChild("td")
                            .addText("ClipBoard").end()
                            .end();
                } 
                while (cursor.moveToNext());

                table.finish();
            } //end if 
            else 
            {
                resp.setContentType("text/plain");
                resp.getWriter().print("No data");
            }
        }
    }

    private static final String ANDROID_CONTEXT_NAME = "net.shuttleplay.shuttle.context";

    private String shortString(String text)
    {
        if (text.length() > 20)
        {
            return text.substring(0,17) + "...";
        }
        return text;
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
        int id = res.getIdentifier("qrecorder","string", context.getPackageName());
        return res.getString(id);
    }

    QrHistory mQrs;
    String mPath;
    boolean mInited;
}
