import java.net.*;
import java.io.*;
import java.util.*;


public class Client{
	private Socket socket_send = null;
	private Socket socket_receive= null;
	private String username;
	private BufferedReader receive_msg= null;
	private BufferedReader sent_ack= null;
	private BufferedWriter send_msg= null;
	private BufferedWriter received_ack= null;
	private BufferedReader stdin_reader= null;
	private BufferedWriter stdout_writer= null;
	private Thread send_thread;
	private Thread receive_thread;
	private Boolean is_active;


	Client(String username, String address, int port1, int port2) {
		try{
			this.username = username;
			// first establish TCP for sending msgs
			socket_send = new Socket(address, port1);
			// then establish TCP for receiving msgs
			socket_receive = new Socket(address, port2);
			receive_msg = new BufferedReader(new InputStreamReader(socket_receive.getInputStream()));
			received_ack = new BufferedWriter(new OutputStreamWriter(socket_receive.getOutputStream()));
			sent_ack = new BufferedReader(new InputStreamReader(socket_send.getInputStream()));
			send_msg = new BufferedWriter(new OutputStreamWriter(socket_send.getOutputStream()));
			stdin_reader = new BufferedReader(new InputStreamReader(System.in));
			stdout_writer = new BufferedWriter(new OutputStreamWriter(System.out));

			send_thread = new Thread(this.new sendMessage());
			receive_thread = new Thread(this.new receiveMessage());
			send_thread.start();
			receive_thread.start();
		}
		catch (UnknownHostException u){
			System.out.println("C 1");
			u.printStackTrace();
		}
		catch(IOException i){
			System.out.println("C 2");
			i.printStackTrace();
		}


	}
	
	
	class sendMessage implements Runnable{
		
		public void run(){
			try{
			//registering for sending messages.
			
			while(true){
				
				String reg_send = "REGISTER TOSEND "+(username);
				send_msg.write(reg_send);
				send_msg.newLine();
				send_msg.newLine();
				send_msg.flush();
				String s = sent_ack.readLine();
				if (sent_ack.read()=='\n'){
						if (s.equals("REGISTERED TOSEND " + username)){
							stdout_writer.write("Successfully registered to send messages");
							stdout_writer.newLine();
							//stdout_writer.newLine();
							stdout_writer.flush();
							break;
						}
						else{
							//errorc
							//could not register
						}
				}
				else{
					//error
				}
				stdout_writer.write(s);
				stdout_writer.write("Re-enter your username: ");
				stdout_writer.flush();
				username = stdin_reader.readLine();
			}
			//actual communication
			while(true){
				String s1 = stdin_reader.readLine();
				int error = 0;
				if (s1.length()<=5){
					error = 1;
				}
				else if (s1.equals("UNREGISTER")){
					send_msg.write(s1);
					send_msg.newLine();
					send_msg.newLine();
					send_msg.flush();

					String sack = sent_ack.readLine();
					if (sent_ack.read()!='\n'){
						error = 2;
					}
					else{
						stdout_writer.write(sack);
						stdout_writer.newLine();
						stdout_writer.flush();
						if (sack.equals("UNREGISTERED")){
							break;
						}
					}
				}
				else if (s1.charAt(0)=='@'){
					String[] mesg = s1.split(":");
					if (mesg.length!=2){
						error = 1;
					}
					else if (mesg[0].length()<=1 || mesg[1].length()<=1){
						error = 1;
					}
					else{
						String user = mesg[0].substring(1);
						// System.out.println(user);
						String message = mesg[1].substring(1);
						// System.out.println(message);

						send_msg.write("SEND "+user);
						// send_msg.write(user);
						send_msg.newLine();
						send_msg.write("Content-length: ");
						send_msg.write(Integer.toString(message.length()));
						send_msg.newLine();
						send_msg.newLine();
						send_msg.write(message);
						send_msg.newLine();
						send_msg.newLine();
						send_msg.flush();
						String sack = sent_ack.readLine();
						if(sent_ack.read()=='\n'){
							stdout_writer.write(sack);
							stdout_writer.newLine();
							stdout_writer.flush();
							// if (sack.charAt[0]!='S' && sack.length()<=5){
							// 	stdout_writer.write(sack);
							// }
							// if (sack.substring(0,5).equals("SENT ")){
							// 	if (sack.substring(5).equals(user)){
							// 		//ack received
							// 		//display custom message
							// 		stdout_writer.write("SUCCESSFULLY SENT\n");
							// 		stdout_writer.flush();
							// 	}
							// 	else{
							// 		stdout_writer.write("SENT TO BAD USER\n");
							// 		stdout_writer.flush();
							// 		//ack incorrectly received
							// 	}
							// }
							// else if (sack.equals("ERROR 102 Unable to send")){
								// stdout_writer.write("Error, Unable to send\n");
								
								//header incorrect
							
						}
						else{
							stdout_writer.write("Bad Response from server\n");
							stdout_writer.flush();
					}
					}
				}
				else{
					error = 1;
					//error foramt incorrect
				}
				switch(error){
					case 1:
						stdout_writer.write("Incorrect format, please type again");
						stdout_writer.flush();
						break;
					case 2:
						stdout_writer.write("Bad Response from server\n");
						stdout_writer.flush();
				}
			}
			
			// unregistered
			socket_send.close();
			socket_receive.close();
		}
		catch(Exception e){
			System.out.println("Currently unhandled");
			e.printStackTrace();
			
		}
		}
	}
	class receiveMessage implements Runnable{
		public void run(){
			try{
			while(true){
				String reg_rcv = "REGISTER TORECV "+(username);
				received_ack.write(reg_rcv);
				received_ack.newLine();
				received_ack.newLine();
				received_ack.flush();
				String s = receive_msg.readLine();
				// System.out.println(s);
				if (receive_msg.read()=='\n'){
						// System.out.println(s);
						if (s.equals("REGISTERED TORECV " + username)){
							stdout_writer.write("Successfully registered to receive messages\n");
							
							stdout_writer.flush();
							break;
						}
						else{
							//error
						}
				}
				else{
					//error
				}
				
			}
			//actual loop for receiving messages 
			while(true){
				String s1 = receive_msg.readLine();
				int error = 0;
					if (s1.length()<=8){
						error = 1;
					}

					else if (s1.substring(0,8).equals("FORWARD ")){
						String user = s1.substring(8);
						String ctl = receive_msg.readLine();
						if (receive_msg.read()=='\n'){
							if (ctl.substring(0,16).equals("Content-length: ")){
								int number_chars = Integer.parseInt(ctl.substring(16));
								char[] char_buf = new char[number_chars];
								receive_msg.read(char_buf,0,number_chars);
								if (receive_msg.read()=='\n'&& receive_msg.read()=='\n'){
									received_ack.write("RECEIVED ");
									received_ack.write(user);
									received_ack.newLine();
									received_ack.newLine();
									received_ack.flush();
									// System.out.println(char_buf);
									
									
									//write to console
									stdout_writer.write("#");
									stdout_writer.write(user);
									stdout_writer.write(": ");
									stdout_writer.write(char_buf,0,number_chars);
									stdout_writer.newLine();
									
									stdout_writer.flush();

								}
								else{
									error =2;
									//error received corrupted message
								}
							}
							else{
								error = 3;
								//bad header from server
							}
						}
						else{
							error = 3;
							// bad header from server
						}
					}
					else{
						error = 1;
						//bad header from server
					}
				
				
				switch(error){
					case 1:
						break;
					case 2:
						break;
					case 3:
						break;
					default:
						
				}
			}
		}
		catch(Exception e){
			System.out.println("Currently unhandled");
			e.printStackTrace();
		}
		}
	}
	
	

	public static void main(String[] args) {
		
		Client client = new Client(args[0],args[1],Integer.parseInt(args[2]),Integer.parseInt(args[3]));

	}
}