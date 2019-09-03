import java.net.*;
import java.io.*;

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



	Client(String username, String address, int port) {
		try{
			this.username = username;
			socket_send = new Socket(address, port);
			socket_receive = new Socket(address, port);
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
	registerToSend() throws {
		String reg_send = (new String("REGISTER TOSEND ")).append(username);
		send_msg.write(reg_send,0,reg_send.length());
		send_msg.newLine();
		send_msg.newLine();
		flush();

	}
	registerToReceive() throws {
		String reg_rcv = (new String("REGISTER TORECV ").append(username));
		

	}
	sendMessage(){

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
		catch(){

		}

	}
}