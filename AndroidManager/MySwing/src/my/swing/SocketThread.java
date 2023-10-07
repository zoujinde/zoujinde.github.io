package my.swing;

import java.io.IOException;
import java.net.ServerSocket;

public class SocketThread extends Thread {
	private int mPort = 0;
	
	public SocketThread(int port){
		this.mPort = port;
	}
	
	public void run(){
		if(mPort<=20000){
			throw new RuntimeException("Port number must > 20000");
		}
		ServerSocket server = null;
		try{ 
			server = new ServerSocket(mPort);
		    while(true){ 
		    	server.accept();//Accept request
		    }
		}catch(Exception   e) { 
			System.out.println("SocketThread : " + e); 
		}finally{
			if(server!=null){
				try {
					server.close();
				} catch (IOException e) {
					System.out.println("ServerSocket close : " + e); 
				}
			}
		}
	} 
		
}
