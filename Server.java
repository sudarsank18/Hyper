import java.io.*;
import java.net.*;

public class Server {

    private ServerSocket serverSocket;
    public Server(ServerSocket ss){
        this.serverSocket = ss;
    }
    public void startServer(){
        try{
            while(!serverSocket.isClosed()){

                System.out.println("waiting for clients....");
                Socket socket = serverSocket.accept();
                System.out.println("new client connected!");
                ClientHandler clientHandler = new ClientHandler(socket);

                Thread clientThread = new Thread(clientHandler);
                clientThread.start();

            }
        }catch(IOException e){
            closeServerSocket();
        }
    }
    public void closeServerSocket(){
        try{
            if(serverSocket!=null){
                serverSocket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws Exception{
        int PORT = 8000;
        ServerSocket ss = new ServerSocket(PORT);

        Server server  = new Server(ss);
        server.startServer();

    }
}
