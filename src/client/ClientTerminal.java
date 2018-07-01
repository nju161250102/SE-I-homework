package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;

public class ClientTerminal {
	
	private Socket client;
	private PrintWriter out;

	//return if connect successfully
	boolean connect(String host, int port, String user) {
		try {
			client = new Socket(host, port);
			send("Login", user);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	//send information to server.Server, use "over" as end
	void send(String command, String s) throws IOException {
		BufferedReader bufr = new BufferedReader(new StringReader(s));
		out = new PrintWriter(client.getOutputStream(),true);
		out.println(command);
		String line=null;
		while((line=bufr.readLine())!=null){
			out.println(line);
		} 
		out.println("over");	
	}
	
	//get information from server.Server, use "over" as end, including line separator
	String get() throws IOException {
		BufferedReader bufrIn =new BufferedReader(new InputStreamReader(client.getInputStream()));
		String line = null;
		String output = "";
		while (! "over".equals(line = bufrIn.readLine())) {
			if ("".equals(output)) output = line;
			else output += (System.lineSeparator() + line);
		}
		return output;
	}
	
	//close the connection to server.Server
	void close() throws IOException {
		client.close();
	}
}
