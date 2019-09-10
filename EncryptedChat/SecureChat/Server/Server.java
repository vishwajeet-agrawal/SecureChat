
import java.io.*;


public class Server{
    public Server(int a, int b, int mode){
        switch(mode){
            case 1:
                new Server1(a,b);
                break;
            case 2:
                new Server2(a,b);
                break;
            case 3:
                new Server3(a,b);
                break;
            default:
                System.out.println("Enter valid mode, 1 for plaintext, 2 for encrypted and 3 for signatured");

        }
    }
    public static void main(String[] args){
        new Server(Integer.parseInt(args[0]),Integer.parseInt(args[1]),Integer.parseInt(args[2]));
    }
}