import java.io.*;

public class Client{
    public Client(String username, String host_ip, int port1, int port2, int mode) {
        switch(mode){
            case 1:
                new Client1(username,host_ip,port1,port2);
                break;
            case 2:
                new Client2(username,host_ip,port1,port2);
                break;
            case 3:
                new Client3(username,host_ip,port1,port2);
                break;
            default:
                System.out.println("Enter valid mode, 1 for plaintext, 2 for encrypted and 3 for signatured");

        }
    }
    public static void main(String[] args){
        new Client(args[0],args[1],Integer.parseInt(args[2]),Integer.parseInt(args[3]),Integer.parseInt(args[4]));
    }
}