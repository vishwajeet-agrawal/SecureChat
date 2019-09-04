import java.net.*;
import java.io.*;
import java.util.*;

public class Server{
	public static void main(String[] args){
		new Server();
	}
	private String ip_address = "localhost";
	// private ConcurrentHashMap<String,User> speaking_users = HashMap();
	private ConcurrentHashMap<String,User> Allusers = ConcurrentHashMap();
	private HashSet<String> users;
	// private ConcurrentHashMap<InetAddress,String> IpToUser = HashMap();
	private ServerSocket speaking_socket = null;
	private ServerSocket listening_socket = null;
	private int port_send;
	private int port_receive;
	private Thread send_thread;
	private Thread receive_thread;
	private ConcurrentHashMap<String,ClientHandlerForReceive> receive_users = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String,ClientHandlerForSend> send_users = new ConcurrentHashMap<>();
	private HashSet<ClientHandlerForReceive> receive_handlers = new HashSet<>();
	private HashSet<ClientHandlerForSend> send_handlers = new HashSet<>();

	class ReceiveSocket implements Runnable{
		public void run(){
			while(true){
				Socket s = Server.this.listening_socket.accept();
				BufferedReader ack_stream = new BufferedReader(new InputStreamReader(s.getInputStream()));
				BufferedWriter message_stream = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
				ClientHandlerForReceive t = new ClientHandlerForReceive(s,ack_stream,message_stream);
				Server.this.receive_sockets.add(t);
				t.start();
			}
		}
	}
	class SendSocket implements Runnable{	
		public void run(){
			while(true){
				Socket s = Server.this.speaking_socket.accept();
				BufferedReader message_stream = new BufferedReader(new InputStreamReader(s_send.getInputStream()));
				BufferedWriter ack_stream = new BufferedWriter(new OutputStreamWriter(s_send.getOutputStream()));
				ClientHandlerForSend t = new ClientHandlerForSend(s,message_stream,ack_stream);
				Server.this.send_sockets.add(t);
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
		Server(5001,5002);
	}	
	
	class ClientHandlerForSend extends Thread{
		Socket s;
		BufferedReader messg;
		BufferedWriter ack;
		String username = new String();
		// PipedInputStream pis; // receiving acknowledges of sending messages to other from other users reading end
		// PipedOutputStream pos;	// writing end
		ClientHandlerForSend(Socket s, BufferedReader br, BufferedWriter bw){
			this.s = s;
			this.mesg = br;
			this.ack = bw;
			// pis = new PipedInputStream();
			// pos = new PipedOutputStream();
			// pis.connect(pos);
			// pos.connect(pis);

		}

		public void run(){
			// loop for registering the user to send
			while(true){
				String s1 = messg.readLine();
				int error=0;
				if (messg.read()=='\n'){
					if (s1.substring(0,16) == "REGISTER TOSEND "){
						String usr = s1.substring(16);
						if (Server.this.send_users.containsKey(usr)){
							//error user already present
						}
						else if (Server.this.checkUsernameWellFormed(usr)){
							Server.this.send_users.put(usr,this);
							if (Server.this.receive_users.containsKey(usr)){
								Server.this.users.put(usr);
							}
							username = usr;
							//success 
							break;
						}
						else{
							error = 100; //user malformed
						}

					}
					else{
						error = 101;
					}
				}
				else{
					error = 101;
				}
				switch(error){
					case 101:
						ack.write("ERROR 101 No user registered");
						ack.newLine();
						ack.newLine();
						ack.flush();
						break;
					case 100:
						ack.write("ERROR 100 Malformed username");
						ack.newLine();
						ack.newLine();
						ack.flush();
						break;
					case 102:

					default:

				}
			}

			// loop for actual communication
			while(true){
				String s1 = messg.readLine();
				int error=0;
				if (messg.read()=='\n'){
					if (s1.substring(0,5)=="SEND "){
						String usr_to_receive = s1.substring(5);
						if (Server.this.users.contains(usr_to_receive)){
							s1 = messg.readLine();
							if (s1.substring(0,16)=="Content-Length: "){
								if (messg.read()=='\n'){
									int number_chars = Integer.parseInt(s1.substring(17));
									char[] msg_to_send = new char[number_chars];
									messg.read(msg_to_send,0,number_chars);
									// now find the user to send the message to and send him the message
									ClientHandlerForReceive receive_end = receive_users.get(usr_to_receive);
									//acquire its lock
									receive_end.lock_stream.lock();
									receive_end.mesg.write("FORWARD ");
									receive_end.mesg.write(username);
									receive_end.mesg.newLine();
									receive_end.mesg.write("Content-Length: ");
									receive_end.mesg.write(Integer.toString(number_chars));
									receive_end.mesg.newLine();
									receive_end.mesg.newLine();
									receive_end.mesg.write(msg_to_send,0,number_chars);
									receive_end.mesg.flush();
									//then reveive ack
									String ack_m = receive_end.ack.readLine();
									if (receive_end.ack.read()=='\n'){
										if (ack_m.substring(0,9)=="RECEIVED "){
											if (ack_m.substring(9)==username){
												ack.write("SENT ");
												ack.write(usr_to_receive);
												ack.newLine();
												ack.newLine();
												ack.flush();
											}
											else{
												error = 102;
											}
										}
										else{
											error = 102;
										}
									}
									else{
										//error bad ack
										error = 102;
									}
									//release the lock
									receive_end.lock_stream.unlock();
								}
								else{
									error = 103;
								}
							}
							else{
								error = 103;
							}
						}
						else{
							//error user to send message not found
							error = 101;
						}
					}
					else{
						error = 103;
					}
				}
				else{
					error = 103;
				}
				switch(error){
					case 101:
						ack.write("ERROR 101 No user registered");
						ack.newLine();
						ack.newLine();
						ack.flush();
						break;
					case 102:
						ack.write("ERROR 102 Unable to send");
						ack.newLine();
						ack.newLine();
						ack.flush();
						break;
					case 103:
						ack.write("Error 103 Header incomplete");
						ack.newLine();
						ack.newLine();
						ack.flush();
						break;
					default:

				}
			}			
		}
	}

	
	class ClientHandlerForReceive extends Thread{
		Socket s;
		Lock lock_stream;
		BufferedReader ack; //coming from client
		BufferedWriter mesg; //writing to client
		String username = new String();
		// PipedInputStream pis;	// messages coming from other users to be read from here.
		// PipedOutputStream pos; 	// message coming from other users to be written to this stream.
		ClientHandlerForReceive(Socket s,BufferedReader br,BufferedWriter bw){
			this.s = s;
			this.ack = br;
			this.mesg = bw;
			lock = new Lock();
			lock_stream.lock();
			// pis = new PipedInputStream();
			// pos = new PipedOutputStream(pis);
			// pis.connect(pos);
		}
		public void run(){
			while(true){
				String s1 = ack.readLine();
				if (mesg.read()=='\n'){
					if (s1.substring(0,16) == "REGISTER TORECV "){
						String usr = s1.substring(16);
						if (Server.this.receive_users.containsKey(usr)){
							//error user already registered
						}
						else if (Server.this.checkUsernameWellFormed(usr)){
							Server.this.receive_users.put(usr,this);
							if (Server.this.send_users.containsKey(usr)){
								Server.this.users.add(usr);
							}
							username = usr;
							//success 
							break;
						}
						else{
							error = 100; //user malformed
						}
					}
					else{
						error = 101;
					}
				}
				else{
					error = 101; //no user registered
				}
				switch(error){
					case 101:
						ack.write("ERROR 101 No user registered");
						ack.newLine();
						ack.newLine();
						ack.flush();
						break;
					case 100:
						ack.write("ERROR 100 Malformed username");
						ack.newLine();
						ack.newLine();
						ack.flush();
						break;
				}
			}


	}
}
	Boolean checkUsernameWellFormed(String usr){
		Boolean check = true;
		for(int i=0;i<usr.length();i++){
			char x = usr.charAt(i);
			if ((x >= 48 && x<=57) || (x>=65 && x<=90) || (x>=97 && x<=122)){
				check = check && true;
			}
		}
		return check;
	}	

}
