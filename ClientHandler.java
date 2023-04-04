import java.net.*;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
public class ClientHandler implements Runnable{

    public static ArrayList<ClientHandler> clientHandlers= new ArrayList<>();

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    public ClientHandler(Socket socket){
        try{
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            String clientPassword = bufferedReader.readLine();
            for(ClientHandler clientHandler: clientHandlers){
                if(clientUsername.equals(clientHandler.getName())){
                    throw new AuthenticationFailedException();
                }
            }
            authenticateClient(clientUsername, clientPassword);
            //send authentication successful
            bufferedWriter.write("authentication successful!");
            bufferedWriter.newLine();
            bufferedWriter.flush();
            clientHandlers.add(this);
            System.out.print("[");
            for(ClientHandler clientHandler: clientHandlers){
                System.out.print(clientHandler.getName()+", ");
            }
            System.out.print("]\n");
        }
        catch(IOException e){
            closeCurrentClient(socket, bufferedReader, bufferedWriter);
        }
        catch(AuthenticationFailedException ae){
            //send authentication failed
            try{
                bufferedWriter.write("authentication failed");
                bufferedWriter.newLine();
                bufferedWriter.flush();
                closeCurrentClient(socket, bufferedReader, bufferedWriter);
            }
            catch(IOException e){
                e.printStackTrace();
            }

        }
    }

    public String getName(){
        return this.clientUsername;
    }

    public static void authenticateClient(String username, String password) throws AuthenticationFailedException{
        Boolean authenticated = false;

        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/user", "root", "#05nov2002");

            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery("select password from information where username like \"" + username + "\"");

            if(!rs.next()){
                throw new AuthenticationFailedException();
            }
            else{
                if(rs.getString(1).equals(password)){
                    authenticated = true;
                }
                else{
                    throw new AuthenticationFailedException();
                }
            }

        }catch(SQLException e){
            e.printStackTrace();
        }

        if(!authenticated) {
            throw new AuthenticationFailedException();
        }
    }
    @Override
    public void run(){
        while(socket.isConnected()){
            try{
                String messageFromClient = bufferedReader.readLine();
                String[] message = messageFromClient.split("#", 2);
                ClientHandler client = null;
                for(ClientHandler clientHandler: clientHandlers){
                    if(clientHandler.clientUsername.equals(message[0])){
                        client = clientHandler;
                        break;
                    }
                }
                if(client != null){
                    sendMessage(client, message[1]);
                }
                else{
                    bufferedWriter.write("User is not online!");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            }catch(IOException e){
                closeCurrentClient(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }
    public void closeCurrentClient(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeClientHandler();
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
    public void sendMessage(ClientHandler clientHandler, String message){
        try {
            clientHandler.bufferedWriter.write(clientUsername+": "+message);
            clientHandler.bufferedWriter.newLine();
            clientHandler.bufferedWriter.flush();
        }catch(IOException e){
            closeCurrentClient(socket, bufferedReader, bufferedWriter);
        }
    }
    public void removeClientHandler(){
        clientHandlers.remove(this);

    }

}
