package net.shuttleplay.shuttle.appshare;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.shuttleplay.shuttle.common.TagWriter;

public class MainUiServlet extends HttpServlet
{

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        //super.doGet(req,resp);
        resp.setContentType("text/html");
        
        PrintWriter out = resp.getWriter();
        
        TagWriter html = new TagWriter(out);
        html.setIndent(2)
           .addChild("head")
                .addChild("title")
                    .addText("Main view of Trend Box").end()
                    .end()
           .addChild("body")
                .addChild("B")
                    .addText("Hello world for Trend Box!").end()
                .end()
                .addChild("table")
                    .addChild("tr")
                        .addChild("th")
                            .addText("")
            .finish();
    }

}
