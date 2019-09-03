import java.net.*;
import java.io.*;
import java.util.*;

public class Server{
	private String ip_address = 'localhost';
	// private ConcurrentHashMap<String,User> speaking_users = HashMap();
	private ConcurrentHashMap<String,User> Allusers = ConcurrentHashMap();
	// private ConcurrentHashMap<InetAddress,User> AllUsers = HashMap();
	private ServerSocket speaking_socket = null;
	private ServerSocket listening_socket = null;
	private int port_send;
	private int port_receive;
	private Thread send_thread;
	private Thread receive_thread;
	private ConcurrentHashMap<InetAddress,Thread> receive_sockets = new ConcurrentHashMap<>();
	private ConcurrentHashMap<InetAddress,Thread> send_sockets = new ConcurrentHashMap<>();

	class ReceiveSocket implements Runnable{
		void run(){
			while(true){
				Socket s = Server.this.listening_socket.accept();
				BufferedReader ack_stream = new BufferedReader(new InputStreamReader(s.getInputStream()));
				BufferedWriter message_stream = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
				Thread t = new Thread(new ClientHandlerForReceive(s,ack_stream,message_stream));
				Server.this.receive_sockets.put(s.getInetAddress(),t);
				t.start();
			}
		}
	}
	class SendSocket implements Runnable{	
		void run(){
			while(true){
				Socket s = Server.this.speaking_socket.accept();
				BufferedReader message_stream = new BufferedReader(new InputStreamReader(s_send.getInputStream()));
				BufferedWriter ack_stream = new BufferedWriter(new OutputStreamWriter(s_send.getOutputStream()));
				Thread t = new Thread(new ClientHandlerForSend(s,message_stream,ack_stream));
				Server.this.send_sockets.put(s.getInetAddress(),t);
				t.start();
			}		
		}
	}

	Server(int port_send, int port_receive){
		// accepting connections on port 
		this.port_send = port_send;
		this.port_receive = port_receive;
		this.speaking_socket = new ServerSocket(port_send);
		this.listening_socket = new ServerSocket(port_receive);
		this.send_thread = new Thread(this.new SendScoket());
		t1.start();
		this.receive_thread = new Thread(this.new ReceiveSocket());
		t2.start();
	}
	Server(){
		// 5001 for send and 5002 for receive
		this = Server(5001,5002);
	}	
	
	class ClientHandlerForSend implements Runnable{
		Socket s;
		BufferedReader messg;
		BufferedWriter ack;
		ClientHandlerForSend(Socket s, BufferedReader br, BufferedWriter bw){
			this.s = s;
			this.mesg = br;
			this.ack = bw;
		}
		void run(){
			String s1 = messg.readLine();
			if (messg.read()=='\n'){
				if (s1.substring(0,16) == "REGISTER TOSEND "){
					String usr = s1.substring(16);

				}
				
			}
		}
	}

	
	class ClientHandlerForReceive implements Runnable{
		Socket s;
		BufferedReader ack;
		BufferedWriter mesg;
		ClientHandlerForReceive(s,br,bw){
			this.s = s;
			this.ack = br;
			this.mesg = bw;
		}
		void run(){
			String s1 = ack.readLine();
			if (mesg.read()=='\n'){
				if (s1.substring(0,16) == "REGISTER TORECV"){
					String usr = s1.substring(16);
				}
			}
		}	

	}
	public static void main(String[] args){
		new Server();
	}

}



class User extends Thread{
	private String username;
	private Socket socket;
	private BufferedWriter bw;
	private BufferedReader br;
	User(){

	}
	void run(){

	}

}