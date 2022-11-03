import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{

	public static ArrayList<ClientHandler> clientHandlers=new ArrayList<>();
	private Socket socket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private String clientUserName;
	
	public ClientHandler (Socket socket) {
		try {
			this.socket=socket;
			this.bufferedWriter=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.clientUserName=bufferedReader.readLine();
			clientHandlers.add(this);
			broadcastMessage("SERVER: "+clientUserName+" has entered the chat!");
		}
		catch(IOException e) {
			closeAll(socket,bufferedReader,bufferedWriter);
		}
	}

	@Override
	public void run() {
		String msg;
		
		while(socket.isConnected()) {
			try {
				msg=bufferedReader.readLine();
				broadcastMessage(msg);
			}
			catch(IOException e) {
				closeAll(socket,bufferedReader,bufferedWriter);
				break;
			}
		}
	}

	private void broadcastMessage(String msg) {
		for(ClientHandler clientHandler:clientHandlers) {
			try {
				if(!clientHandler.clientUserName.equals(clientUserName)) {
					clientHandler.bufferedWriter.write(msg);
					clientHandler.bufferedWriter.newLine();
					clientHandler.bufferedWriter.flush();
				}
			}
			catch(IOException e) {
				closeAll(socket,bufferedReader,bufferedWriter);
			}
		}
	}
	
	public void removeClientHandler() {
		clientHandlers.remove(this);
		broadcastMessage("SERVER: "+ clientUserName +" has left the chat!");
	}

	private void closeAll(Socket socket2, BufferedReader bufferedReader2, BufferedWriter bufferedWriter2) {
		removeClientHandler();
		try {
			if(bufferedReader!=null)
				bufferedReader.close();
			if(bufferedWriter!=null)
				bufferedWriter.close();
			if(socket!=null)
				socket.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
}
