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



	Client(String username, String address, int port1, int port2) {
		try{
			this.username = username;
			// first establish TCP for sending msgs
			socket_send = new Socket(address, port1);
			// then establish TCP for receiving msgs
			socket_receive = new Socket(address, port2);
			receive_msg = new BufferedReader(new InputStreamReader(socket_receive.getOutputStream()));
			received_ack = new BufferedWriter(new OutputStreamWriter(socket_receive.getInputStream()));
			sent_ack = new BufferedReader(new InputStreamReader(socket_send.getOutputStream()));
			send_msg = new BufferedWriter(socket_send.getInputStream());
			stdin_reader = new BufferedReader(new InputStreamReader(System.in));
			stdout_writer = new BufferedWriter(new OutputStreamWriter(System.out));
		}
		catch (UnknownHostExceptio u){

		}
		catch(IOException i){

		}


	}
	registerToSend()  {
		String reg_send = (new String("REGISTER TOSEND ")).append(username);
		send_msg.write(reg_send,0,reg_send.length());
		send_msg.newLine();
		send_msg.newLine();
		flush();
		String s = sent_ack.readLine();
		if (sent_ack.read()=='\n'){

		}

	}
	registerToReceive()  {
		String reg_rcv = (new String("REGISTER TORECV ").append(username));
		received_ack.write(reg_rcv,0,reg_rcv.length());
		received_ack.newLine();
		received_ack.newLine();
		flush();

	}
	
	class sendMessage implements Runnable{
		String msg = stdin_reader.readLine();
		public void run(){
			while(true){
				String s1 = stdin_reader.readLine();
				if (s1.charAt[0]=='@'){
					String[] mesg = s1.split(":");
					String user = mesg[0].substring(1);
					String message = mesg[1].substring(1);
					String mesg_send_form = (new String("SEND ")).append(user);
					send_msg.write(mesg_send_form);
					send_msg.newLine();
					send_msg.write((new String("Content-length: ")));
					send_msg.write(Integer.toString(message.length()));
					send_msg.newLine();
					send_msg.newLine();
					send_msg.write(message);
					send_msg.newLine();
					send_msg.newLine();
					send_msg.flush();
					String sack = send_ack.readLine();
					if(send_ack.read()=="\n"){
						
						if (sack.substring(0,5)=="SENT "){
							if (sack.substring(6)==user){
								//ack received
								//display custom message
								stdout_writer.write("SUCCESSFULLY SENT\n");
								stdout_writer.flush();
							}
							else{
								stdout_writer.write("SENT TO BAD USER\n");
								stdout_writer.flush();
								//ack incorrectly received
							}
						}
						else if (sack=="ERROR 102 Unable to send"){
							stdout_writer.write("Error, Unable to send\n");
							stdout_writer.flush();
							//header incorrect
						}
					}
					else{
						stdout_writer.write("Bad Response from server\n");
						stdout_writer.flush();
					}

				}
				else{
					stdout_write.write("Incorrect format, please type again");
					stdout_write.flush();
					//error foramt incorrect
				}
			}
		}
	}
	class receiveMessage implements Runnable{
		public void run(){
			while(true){
				String s1 = receive_msg.readLine();
				if (receive_msg.read()=='\n'){
					if (s1.substring(0,8)=="FORWARD "){
						String user = s1.substring(8);
						String ctl = receive_msg.readLine();
						if (receive_msg.read()=='\n'){
							if (ctl.substring(0,16)=="Content-length: "){
								int number_chars = Integer.parseInt(ctl.substring(16));
								char[] char_buf = new char[number_chars];
								receive_msg.read(char_buf,0,number_chars);
								if (receive_msg.read()=='\n'&& receive_msg.read()=='\n'){
									received_ack.write("RECEIVED ");
									received_ac
									
									
									//write to console
									stdout_writer.write("#");
									stdout_writer.write(user);
									stdout_writer.write(": ");
									stdout_writer.write(char_buf,0,number_chars);
									stdout_writer.newLine();
									
									stdout.flush();

								}
								else{
									//error received corrupted message
								}
							}
							else{
								//bad header from server
							}
						}
						else{
							// bad header from server
						}
					}
					else{
						//bad header from server
					}
				}	
				else{
					//error unrecognized response from server
				}
			}
		}
	}
	
	receiveMessage(){

	}
	sendReceivedAck(){

	}
	receiveSentAck(){

	}

	public static void main(String[] args) {
		try{
			client = new Client(args[1],args[2],StringToInt(args[3]));
			registerToSend();
			registerToReceive();
			



		}
		catch(Exception e){

		}

	}
}