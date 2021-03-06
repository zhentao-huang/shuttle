package net.shuttleplay.shuttle.app.handler;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.ByteArrayISO8859Writer;
import org.eclipse.jetty.util.StringUtil;

import android.util.Log;


public class DefaultHandler extends org.eclipse.jetty.server.handler.DefaultHandler
{

    public static final String TAG = "Shuttle";
    
    public DefaultHandler()
    {
       super();
    }

    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        if (response.isCommitted() || baseRequest.isHandled())
            return;

        baseRequest.setHandled(true);

        String method=request.getMethod();

    
        /*
        if (method.equals(HttpMethods.GET) && request.getRequestURI().equals("/main"))
        {
              HttpServlet servlet = new net.shuttleplay.shuttle.app.servlet.MainUiServlet();
              servlet.service(request, response);
              return;
        }
        */
        
        if (!method.equals(HttpMethods.GET) || !request.getRequestURI().equals("/"))
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return; 
        }

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentType(MimeTypes.TEXT_HTML);

        ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer(1500);

        String uri=request.getRequestURI();
        uri=StringUtil.replace(uri,"<","&lt;");
        uri=StringUtil.replace(uri,">","&gt;");

        writer.write("<HTML>\n<HEAD>\n<TITLE>Welcome to Shuttle");
        writer.write("</TITLE>\n<BODY>\n<H2>Welcome to Shuttle</H2>\n");
        writer.write("<p>Shuttle is running successfully.</p>");

        Server server = getServer();
        Handler[] handlers = server==null?null:server.getChildHandlersByClass(ContextHandler.class);

        int i=0;
        for (;handlers!=null && i<handlers.length;i++)
        {
            if (i == 0)
            {
                writer.write("<p>You had install " + handlers.length + " plugin(s)</p>");
                writer.write("<p>So you can share : </p>");
            }

            ContextHandler context = (ContextHandler)handlers[i];
            if (context.isRunning())
            {
                StringBuilder builder = new StringBuilder();
                if (context.getVirtualHosts()!=null && context.getVirtualHosts().length>0)
                {
                    builder.append("http://"+context.getVirtualHosts()[0]+":"+request.getLocalPort());
                }
                String contextPath = context.getContextPath();
                builder.append(contextPath);
                Log.w(TAG, "ContextPath = " + contextPath);
                if (contextPath.equals("/webdav"))
                {
                    writer.write("<li><img src=\"" + builder.toString() + "/Shuttle/webapps/webdav/icon.png\" width=\"72\" height=\"72\"/><a href=\"");
                }
                else
                {
                    writer.write("<li><img src=\"" + builder.toString() + "/icon.png\" width=\"72\" height=\"72\"/><a href=\"");
                }
                writer.write(builder.toString());
                if (context.getContextPath().length()>1 && context.getContextPath().endsWith("/"))
                    writer.write("/");
                writer.write("\">");
                writer.write(context.getDisplayName());
//                writer.write(context.getContextPath());
//                if (context.getVirtualHosts()!=null && context.getVirtualHosts().length>0)
//                    writer.write("&nbsp;@&nbsp;"+context.getVirtualHosts()[0]+":"+request.getLocalPort());
//                writer.write("&nbsp;--->&nbsp;");
//                writer.write(context.toString());
                writer.write("</a></li>\n");
            }
            else
            {
                writer.write("<li>");
                writer.write(context.getContextPath());
                if (context.getVirtualHosts()!=null && context.getVirtualHosts().length>0)
                    writer.write("&nbsp;@&nbsp;"+context.getVirtualHosts()[0]+":"+request.getLocalPort());
                writer.write("&nbsp;--->&nbsp;");
                writer.write(context.toString());
                if (context.isFailed())
                    writer.write(" [failed]");
                if (context.isStopped())
                    writer.write(" [stopped]");
                writer.write("</li>\n");
            }
            
            if (i == handlers.length -1)
                writer.write("</ul>\n");
        }
        
        if (i == 0)
            writer.write("<p>There are currently no apps deployed.</p>");

        for (int j=0;j<10;j++)
            writer.write("\n<!-- Padding for IE                  -->");

        writer.write("\n</BODY>\n</HTML>\n");
        writer.flush();
        response.setContentLength(writer.size());
        OutputStream out=response.getOutputStream();
        writer.writeTo(out);
        out.close();
    }

    
}
