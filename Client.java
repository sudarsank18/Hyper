import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    public Client(Socket socket, String username, String password) throws AuthenticationFailedException{
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;

            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            bufferedWriter.write(password);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            String authenticationMessage = bufferedReader.readLine();
            if(authenticationMessage.equals("authentication failed")){
                closeClient(socket, bufferedReader, bufferedWriter);
                throw new AuthenticationFailedException();
            }else{
                System.out.println(authenticationMessage);
            }

        }catch(IOException e){
            closeClient(socket, bufferedReader, bufferedWriter);
        }
    }
    public void sendMessage(){
        try{
            Scanner s = new Scanner(System.in);
            while(socket.isConnected()) {
                String message = s.nextLine();
                bufferedWriter.write(message);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        }
        catch(IOException e){
            closeClient(socket, bufferedReader, bufferedWriter);
        }
    }
    public void listenForMessage(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                String recievedMessage;
                while(socket.isConnected()){
                    try{
                        recievedMessage = bufferedReader.readLine();
                        System.out.println(recievedMessage);
                    }catch(IOException e){
                        closeClient(socket, bufferedReader, bufferedWriter);
                        break;
                    }
                }
            }
        }).start();
    }
    public void closeClient(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        try{
            if(socket != null){
                socket.close();
            }
            if(bufferedWriter != null){
                bufferedWriter.close();
            }
            if(bufferedReader != null){
                bufferedReader.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        while(true) {
            System.out.println("Enter username: ");
            String username = s.nextLine();

            System.out.println("Enter password: ");
            String password = s.nextLine();
            try {
                String HOST = "localhost";
                int PORT = 8000;
                Socket socket = new Socket(HOST, PORT);
                Client client = new Client(socket, username, password);

                client.listenForMessage();
                client.sendMessage();
            } catch (AuthenticationFailedException ae) {
                System.out.println("wrong credentials!");
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}