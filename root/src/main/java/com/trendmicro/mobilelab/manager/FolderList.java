package com.trendmicro.mobilelab.manager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trendmicro.mobilelab.common.NetUtil;
import com.trendmicro.mobilelab.common.TagWriter;

@SuppressWarnings("serial")
public class FolderList extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		
		if (pathInfo == null || pathInfo.equals("/"))
		{
			
		}
		else if (pathInfo.startsWith("/count"))
		{
			String path = pathInfo.substring("/count/".length());
			File[] files = getChildren(path);
			resp.setContentType("text/plain");
			resp.getWriter().print(files.length);
		}
		else if (pathInfo.startsWith("/list"))
		{
			String path = pathInfo.substring("/list/".length());
			File[] files = getChildren(path);
			resp.setContentType("text/html;charset=UTF-8");
			TagWriter table = new TagWriter(resp.getWriter(), "table");
			table.setIndent(2);
			for (File file : files)
			{
				String url = "/webdav" + path + "/" + file.getName();
				String down = "/flist/down" + path + "/" + file.getName();
				String localip = NetUtil.getLocalIpAddress();
				
				table
					.addChild("tr")
						.addChild("td")
							.addChild("a").addAttr("href", url)
								.addText(file.getName())
								.end()
							.end()
						.addChild("td");
				
				if (file.isDirectory())
				{
					table.last().addText("Folder")
				 			.end()
				 		.addChild("td").addText("&nbsp;");
					String target = "http://" + localip + ":8000" + down;
					String qr = "http://" + localip + ":8000/manager/qr/" + target;
					
					table.last()
						.addChild("td")
							.addChild("a").addAttr("href", qr).addText("QR code");
				}
				else
				{
					table.last().addText("" + file.length())
						.end();
					String target = "http://" + localip + ":8000" + url;
					String qr = "http://" + localip + ":8000/manager/qr/" + target;
					
					table.last()
						.addChild("td")
							.addChild("a").addAttr("href", qr).addText("QR code");
				}
			
			}
			table.finish();
		}
		else if (pathInfo.startsWith("/down"))
		{
			String filename = "/mnt/sdcard" + pathInfo.substring("/down".length());
			
			File file = new File(filename);
			if (file.exists() && file.isFile())
			{
				resp.setContentType("application/octet-stream");
				resp.setContentLength((int) file.length());
				resp.addHeader("Content-Disposition","attachement;filename=" + file.getName());
				InputStream in = new BufferedInputStream(new FileInputStream(file));
				OutputStream out = resp.getOutputStream();
				NetUtil.copyIO(in, out);
			}
		}
	}
	
	private static class FileComparator implements Comparator<File>
	{

		public int compare(File o1, File o2) {
			if (o1.equals(o2))
			{
				return 0;
			}
			else if (o1.isDirectory() == o2.isDirectory())
			{
				return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
			}
			else if (o1.isDirectory())
			{
				return -1;
			}
			
			return 1;
		}
		
	}

	private File[] getChildren(String path)
	{
		ArrayList<File> files = new ArrayList<File>();
		
		File folder = new File("/mnt/sdcard", path);
		
		if (folder.isDirectory())
		{
			File[] l = folder.listFiles();
			Arrays.sort(l, fc);
			return l;
		}
		
		return null; 
	}
	
	private FileComparator fc = new FileComparator();
	
}
