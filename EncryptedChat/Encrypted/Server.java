import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Collections;

public class Server{
	public void print(String s){
		System.out.print(s);
	}
	public static void main(String[] args){
		try{
			new Server();
		}
		catch(Exception e){
			System.out.println("Uncaught exception");
			e.printStackTrace();
		}
	}
	private String ip_address = "localhost";
	// private ConcurrentHashMap<String,User> speaking_users = HashMap();
	//private ConcurrentHashMap<String,User> Allusers = ConcurrentHashMap<>();
	private Set<String> users = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());;
	private ConcurrentHashMap<String,Byte[]> users_pk = new ConcurrentHashMap<>();
	// private ConcurrentHashMap<InetAddress,String> IpToUser = HashMap();
	private ServerSocket speaking_socket = null;
	private ServerSocket listening_socket = null;
	private int port_send = 5001;
	private int port_receive = 5002;
	private Thread send_thread = null;
	private Thread receive_thread = null;
	private ConcurrentHashMap<String,ClientHandlerForReceive> receive_users = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String,ClientHandlerForSend> send_users = new ConcurrentHashMap<>();
	private HashSet<ClientHandlerForReceive> receive_handlers = new HashSet<>();
	private HashSet<ClientHandlerForSend> send_handlers = new HashSet<>();

	class ReceiveSocket implements Runnable{
		public void run(){
			try{
			while(true){
				Socket s = listening_socket.accept();
				BufferedReader ack_stream = new BufferedReader(new InputStreamReader(s.getInputStream()));
				BufferedWriter message_stream = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
				ClientHandlerForReceive t = new ClientHandlerForReceive(s,ack_stream,message_stream);
				receive_handlers.add(t);
				t.start();
			}
		}
		catch(Exception e){
			System.out.println("UC receive_socket");
			e.printStackTrace();
		}
		}
	}
	class SendSocket implements Runnable{	
		public void run(){
			try{
			while(true){
				Socket s = speaking_socket.accept();
				BufferedReader message_stream = new BufferedReader(new InputStreamReader(s.getInputStream()));
				BufferedWriter ack_stream = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
				ClientHandlerForSend t = new ClientHandlerForSend(s,message_stream,ack_stream);
				send_handlers.add(t);
				t.start();
			}
		}
		catch(Exception e){
			System.out.println("UC send_socket");
			e.printStackTrace();
		}		
		}
	}

	Server(int port_send, int port_receive){
		// accepting connections on port
		try{ 
		this.port_send = port_send;
		this.port_receive = port_receive;
		this.speaking_socket = new ServerSocket(port_send);
		this.listening_socket = new ServerSocket(port_receive);
		//Server.SendSocket send_t = this.(new SendSocket());
		this.send_thread = new Thread(this.new SendSocket());
		send_thread.start();
		this.receive_thread = new Thread(this.new ReceiveSocket());
		receive_thread.start();
		}
		catch(Exception e){
			System.out.println("UCE 1");
			e.printStackTrace();
		}
	}
	
	Server(){
		// 5001 for send and 5002 for receive
	
		this(5001,5002);
		
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
			this.messg = br;
			this.ack = bw;
		}

		public void run(){
			try{
			// loop for registering the user to send
			while(true){
				String s1 = messg.readLine();
				// print(s1);//
				int error=0;
				if (messg.read()=='\n'){
					if (s1.length()<=16){
						error = 103;
					}
					else if (s1.substring(0,16).equals("REGISTER TOSEND ")){
						// System.out.println("REgistered to send ");

						String usr = s1.substring(16);
						if (send_users.containsKey(usr)){
							//error user already present
							error = 102;
						}
						else if (checkUsernameWellFormed(usr)){
							send_users.put(usr,this);
							if (receive_users.containsKey(usr)){
								users.add(usr);
							}
							username = usr;
							ack.write("REGISTERED TOSEND "+usr);
							ack.newLine();
							ack.newLine();
							ack.flush();
							// System.out.println("REgistered to send ");
							//success 
							break;
						}
						else{
							error = 100; //user malformed
						}

					}
					else{
						error = 103;
					}
				}
				else{
					error = 103;
				}
				error_message(ack,error);

			}

			// loop for actual communication
			while(true){
				String s1 = messg.readLine();
				// reading until 2 consecutive \n
				// int state_ = 0 ;

				// print(s1);
				// System.out.println(s1.length());
				int error=0;
				if (s1.length()<=5){
					error = 105;
				}
				else if (s1.substring(0,5).equals("SEND ")){
					// print(s1);
					String usr_to_receive = s1.substring(5);
					
						s1 = messg.readLine();
						if (s1.substring(0,16).equals("Content-length: ")){
							if (messg.read()=='\n'){
								int number_chars = Integer.parseInt(s1.substring(16));
								char[] msg_to_send = new char[number_chars+2];
								messg.read(msg_to_send,0,number_chars+2);
								if (!(msg_to_send[number_chars+1]=='\n' && msg_to_send[number_chars]=='\n')){
									error = 102;
								}
								else{
								// now find the user to send the message to and send him the message
									if (users.contains(usr_to_receive)){

									
									ClientHandlerForReceive receive_end = receive_users.get(usr_to_receive);
									// System.out.println(s1);
									//acquire its lock
									receive_end.lock_stream.lock();
									// System.out.println(s1);
									//forward the message
									receive_end.mesg.write("FORWARD ");
									receive_end.mesg.write(username);
									receive_end.mesg.newLine();
									receive_end.mesg.write("Content-length: ");
									receive_end.mesg.write(Integer.toString(number_chars));
									receive_end.mesg.newLine();
									receive_end.mesg.newLine();
									receive_end.mesg.write(msg_to_send,0,number_chars);
									receive_end.mesg.newLine();
									receive_end.mesg.newLine();
									receive_end.mesg.flush();

									//then reveive ack
									String ack_m = receive_end.ack.readLine();
									if (receive_end.ack.read()=='\n'){
										if (ack_m.substring(0,9).equals("RECEIVED ")){
											if (ack_m.substring(9).equals(username)){
												
												// System.out.println("Sent "+username);
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
										error = 107;
									}
									
									//release the lock
									receive_end.lock_stream.unlock();
								}
									else{
										error = 101;
									}
								}
							}
							else{
								error = 103;
							}
						}
						else{
							error = 103;
						}
					
				}
				else if (s1.equals("UNREGISTER") && messg.read()=='\n'){
					users.remove(username);
					send_users.remove(username);
					receive_users.remove(username);

					ack.write("UNREGISTERED");
					ack.newLine();
					ack.newLine();										
					ack.flush();
					
					break;
				}
				else if (s1.substring(0,6).equals("GETPK ")){
					if (messg.read()!='\n'){
						error = 103;
					}
					else{
						String user_to_get = s1.substring(6);
						if (users.contains(user_to_get)){
							String pk = receive_users.get(user_to_get).publicKey;
							ack.write("SENDPK ");
							ack.write(user_to_get);
							ack.newLine();
							ack.write(pk);
							// System.out.print(pk);
							ack.newLine();
							ack.newLine();
							ack.flush();
						}
						else{
							error = 101;
						}
					}
				}
				else{
					error = 103;
				}
				error_message(ack,error);
				
			}			
			System.out.println(username + "deregistered");
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		// void forwardMessage(String user_to_receive, char[] msg_to_send, int number_chars){
			
		// }
	}

	
	class ClientHandlerForReceive extends Thread{
		Socket s;
		ReentrantLock lock_stream = new ReentrantLock();
		BufferedReader ack; //coming from client
		BufferedWriter mesg; //writing to client
		String publicKey;
		String username = new String();
		// PipedInputStream pis;	// messages coming from other users to be read from here.
		// PipedOutputStream pos; 	// message coming from other users to be written to this stream.
		ClientHandlerForReceive(Socket s,BufferedReader br,BufferedWriter bw){
			this.s = s;
			this.ack = br;
			this.mesg = bw;
			
			//lock_stream.lock();
			// pis = new PipedInputStream();
			// pos = new PipedOutputStream(pis);
			// pis.connect(pos);
		}
		public void run(){
			try{
				while(true){
					int error = 0;
					lock_stream.lock();
					String s1 = ack.readLine();

					String pk1 = ack.readLine();

					if (ack.read()=='\n'){
						if (s1.length()<=16){
							error = 101;
						}
						else if (s1.substring(0,16).equals("REGISTER TORECV ")){
							// System.out.println("2");

							if (pk1.length()<=4){
								error = 101;
							}
							else if (pk1.substring(0,3).equals("PK ")){
								// System.out.println("3");

							
								String usr = s1.substring(16);
								if (receive_users.containsKey(usr)){
									//error user already registered
									error = 102;
								}
								else if (checkUsernameWellFormed(usr)){
									// System.out.println("4");

									this.publicKey = pk1.substring(3);
									receive_users.put(usr,this);
									if (send_users.containsKey(usr)){
										users.add(usr);
									}
									username = usr;
									//success 
									mesg.write("REGISTERED TORECV "+usr);
									mesg.newLine();
									mesg.newLine();
									mesg.flush();
									//success 
									break;
								}
								else{
									error = 100; //user malformed
								}
							}
						}
						else{
							error = 103;
						}
					}
					else{
						error = 103; //no user registered
					}
					error_message(mesg,error);
					lock_stream.unlock();
				}	
				lock_stream.unlock();
			}
			catch(Exception e){
				e.printStackTrace();
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
	void error_message(BufferedWriter mesg, int error){
		try{
		switch(error){
			case 100:
				mesg.write("ERROR 100 Malformed username");
				break;
			case 101:
				mesg.write("ERROR 101 User to send message not found");
				break;
			case 102:
				mesg.write("ERROR 102 username already exists");
				break;
			case 103:
				mesg.write("ERROR 103 incomplete header");
				break;
			case 104:
				mesg.write("ERROR 104 Message Corrupted");
				break;
			case 105:
				mesg.write("ERROR 105 Command not found");
				break;
			case 107:
				mesg.write("ERROR 107 unable to send");
				break;
			default:
				return;
		}
		mesg.newLine();
		mesg.newLine();
		mesg.flush();
	}
	catch(Exception e){
		e.printStackTrace();
	}
	}
}
