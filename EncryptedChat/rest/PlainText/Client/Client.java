
import java.net.*;
import java.io.*;
import java.util.*;
// import java.security.KeyFactory;
// import java.security.KeyPair;
// import java.security.KeyPairGenerator;
// import java.security.NoSuchAlgorithmException;
// import java.security.NoSuchProviderException;
// import java.security.PrivateKey;
// import java.security.PublicKey;
// import java.security.SecureRandom;
// import java.security.spec.PKCS8EncodedKeySpec;
// import java.security.spec.X509EncodedKeySpec;
// import java.util.Base64;
// import java.security.MessageDigest;
// import javax.crypto.Cipher;
// import CryptographyExample.*;
// import javax.crypto.SecretKey;
import java.util.concurrent.locks.ReentrantLock;

public class Client{
	private ReentrantLock read_input_lock = new ReentrantLock();
	private ReentrantLock write_lock = new ReentrantLock();
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
	private Boolean get_another_username = false;
	// private Boolean is_active;
	// private byte[] pk = null;
	// private byte[] sk = null;
	

	Client(String username, String address, int port1, int port2) {
		try{

			this.username = username;
			// KeyPair kp = CryptFuncs.generateKeyPair();
			// this.pk = kp.getPublic().getEncoded();
			// this.sk = kp.getPrivate().getEncoded();
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
		catch(Exception e){
			e.printStackTrace();
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
							output_client("Successfully registered to send messages");
							// read_input_lock.lock();
							get_another_username = true;
							// read_input_lock.unlock();
							break;
						}
						else{
							// read_input_lock.lock();
							
							// get_another_username = false;
							// read_input_lock.unlock();
						}
				}
				else{
					// read_input_lock.lock();

					// get_another_username = false;
					// read_input_lock.unlock();
				}
				if (read_input_lock.isHeldByCurrentThread())
					read_input_lock.unlock();
				read_input_lock.lock();
				if (!get_another_username){
					write_lock.lock();
					stdout_writer.write(s);
					stdout_writer.newLine();
					stdout_writer.write("Re-enter your username: ");
					stdout_writer.flush();
					write_lock.unlock();
					username = stdin_reader.readLine();	
				}
				
				// read_input_lock.unlock();
			}
			if (read_input_lock.isHeldByCurrentThread())
					read_input_lock.unlock();
			//actual communication
			
			while(true){
				String s1 = stdin_reader.readLine();
				int error = 0;
				if (s1.length()<=4){
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
						output_client(sack);

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
						if (mesg[1].charAt(0)!=' '){
							error = 1;
						}
						else{
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
								output_client(sack);
							}
							else{
								output_client("Bad Response from server");
							}
						}
					}
				}
				else{
					error = 1;
					//error foramt incorrect
				}
				switch(error){
					case 1:
						write_lock.lock();
						stdout_writer.write("Incorrect format, please type again\n");
						stdout_writer.flush();
						write_lock.unlock();
						break;
					case 2:
						write_lock.lock();
						stdout_writer.write("Bad Response from server\n");
						stdout_writer.flush();
						write_lock.unlock();
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
							output_client("Successfully registered to receive messages");
							// read_input_lock.lock();
							get_another_username = true;
							// read_input_lock.unlock();
							
							break;
						}
						else{
							// read_input_lock.lock();
							// get_another_username = false;
							// read_input_lock.unlock();
						}
				}
				else{
					// read_input_lock.lock();
					// get_another_username = false;
					// read_input_lock.unlock();
				}
				if (read_input_lock.isHeldByCurrentThread())
					read_input_lock.unlock();
				read_input_lock.lock();
				if (!get_another_username){	
					write_lock.lock();
					stdout_writer.write(s);
					stdout_writer.newLine();
					stdout_writer.write("Re-enter your username: ");
					stdout_writer.flush();
					write_lock.unlock();
					username = stdin_reader.readLine();
					// get_another_username = true;
				}
				// read_input_lock.unlock();
			}
			if (read_input_lock.isHeldByCurrentThread())
					read_input_lock.unlock();

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
									write_lock.lock();
									stdout_writer.write("#");
									stdout_writer.write(user);
									stdout_writer.write(": ");
									stdout_writer.write(char_buf,0,number_chars);
									stdout_writer.newLine();
									
									stdout_writer.flush();
									write_lock.unlock();

								}
								else{
									error =2;
									//error received corrupted message
								}
							}
							else{
								error = 1;
								//bad header from server
							}
						}
						else{
							error = 1;
							// bad header from server
						}
					}
					else{
						error = 1;
						//bad header from server
					}
				
				
				switch(error){
					case 2:
						output_client("Error, received corrupted message");
						break;
					case 1:
						output_client("Error, bad header from server");
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
	public void output_client(String msg) throws Exception{
		write_lock.lock();
		stdout_writer.write(msg);
		stdout_writer.newLine();
		stdout_writer.flush();
		write_lock.unlock();
	}
	

	public static void main(String[] args) {
		
		Client client = new Client(args[0],args[1],Integer.parseInt(args[2]),Integer.parseInt(args[3]));

	}
}





