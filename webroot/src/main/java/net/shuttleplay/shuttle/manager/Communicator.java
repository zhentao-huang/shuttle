package net.shuttleplay.shuttle.manager;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Communicator extends HttpServlet{
	public static class Message{
		long id;
		long timestamp;
		String source;
		String content;
		public Message(){
			timestamp = id = System.currentTimeMillis();
		}
	}
	
	public static class Role{
		public Role(String name) {
			this.name = name;
		}
		private String name;
		private LinkedBlockingQueue<Message> messages = new LinkedBlockingQueue<Message>();
		private List<Role> subscribles = new ArrayList<Role>();
		public String getName() {
			return name;
		}
		public Message pollMessage(int timeout){
			try {
				if(timeout == -1){ //no wait
					return messages.poll();
				}
				else if(timeout==0){ //infin
					return messages.poll(Integer.MAX_VALUE,TimeUnit.MINUTES);
				}
				else{
					return messages.poll(timeout,TimeUnit.SECONDS);
				}
			} catch (InterruptedException e) {
				return null;
			}
		}
		public void newMessage(Message m){
			messages.add(m);
		}
		public void addSubscrible(Role r){
			subscribles.add(r);
		}
		public void removeSubscrible(Role r){
			subscribles.remove(r);
		}
		public void sendMessage(String content){
			Message m = new Message();
			m.source = this.getName();
			m.content =  content;
			for(Role r:subscribles){
				r.newMessage(m);
			}
		}
		public void resetMessages()
		{
			messages.clear();
		}
	}
	private static final long serialVersionUID = 19238687844588052L;
	private Map<String,Role> roles = new ConcurrentHashMap<String,Role>();
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		doGet(req,res);
	}
	
	void responseOK(HttpServletResponse res) throws IOException{
		res.getWriter().print("OK");
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		String action = req.getParameter("action");
		if("register".equalsIgnoreCase(action)){
			onRegister(req,res);
		}
		else if("unregister".equalsIgnoreCase(action)){
			onUnregister(req,res);
		}
		else if("whois".equalsIgnoreCase(action)){
			onWhois(req,res);
		}
		else if("send".equalsIgnoreCase(action)){
			onSend(req,res);
		}
		else if("recv".equalsIgnoreCase(action)){
			onRecv(req,res);
		}
		else if("subscrible".equalsIgnoreCase(action)){
			onSubscrible(req,res);
		}
	}

	private void onSubscrible(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String role = req.getParameter("role");
		String dest = req.getParameter("dest");
		
		if(roles.containsKey(role) && roles.containsKey(dest)){
			Role owner = roles.get(role);
			Role that = roles.get(dest);
			that.addSubscrible(owner);
			responseOK(res);
		}
	}

	private void onRecv(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String role = req.getParameter("role");
		String to = req.getParameter("timeout");
		int timeout = -1;
		if(to!=null){
			timeout = Integer.parseInt(to);
		}
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		if(roles.containsKey(role)){
			Role owner = roles.get(role);
			Message m = owner.pollMessage(timeout);
			if(m!=null){
				sb.append("id:"+m.id+",");
				sb.append("timestamp:"+"\""+m.timestamp+"\",");
				sb.append("source:"+"\""+m.source+"\",");
				sb.append("content:"+m.content);
			}
		}
		sb.append("}");
		res.getWriter().println(sb.toString());
	}

	private void onSend(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String role = req.getParameter("role");
		String content = req.getParameter("content");
		if(roles.containsKey(role)){
			Role owner = roles.get(role);
			owner.sendMessage(content);
			responseOK(res);
		}
	}

	private void onWhois(HttpServletRequest req, HttpServletResponse res) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		boolean first = true;
		for(String r:roles.keySet()){
			if(!first){
				sb.append(",");
			}
			else{
				first = false;
			}
			sb.append("\""+r+"\"");
		}
		sb.append("]");
		res.getWriter().println(sb.toString());
	}

	private void onUnregister(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String role = req.getParameter("role");
		if(roles.containsKey(role)){
			Role owner = roles.get(role);
			for(Role r:roles.values()){
				r.removeSubscrible(owner);
			}
			roles.remove(role);
			responseOK(res);
		}
	}

	private void onRegister(HttpServletRequest req, HttpServletResponse res)throws ServletException, IOException {
		String role = req.getParameter("role");
		if(roles.containsKey(role)){
			//throw new ServletException("Role already exists");
			Role owner = roles.get(role);
			owner.resetMessages();
		}
		else{
			Role owner= new Role(role);
			for(Role r:roles.values()){
				r.addSubscrible(owner);
				owner.addSubscrible(r);
			}
			roles.put(role, owner);
		}
		responseOK(res);
	}
}
