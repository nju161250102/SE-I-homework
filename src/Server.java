import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {

		try { 
			ServerSocket serverSocket = new ServerSocket(10080); 
			System.out.println("Server Running ......");
		    Socket socket = null;  
		     
		    while (true){  
		        socket = serverSocket.accept();  
		        ServerThread serverThread = new ServerThread(socket); 
		        //System.out.println("The Number of Clients: " + ServerThread.count); 
		        serverThread.start(); 
		    } 
		} catch (IOException e) { 
		      e.printStackTrace(); 
		}
	}
}
