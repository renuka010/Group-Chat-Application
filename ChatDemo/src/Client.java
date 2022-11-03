import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class Client extends JFrame{
	
	private Socket socket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private PrintWriter printWriter;
	private String userName;
	
	private JLabel heading=new JLabel("Group Chat");
	private JTextArea msgArea=new JTextArea();
	private JTextField msgInput=new JTextField();
	private Font font=new Font("Roboto",Font.PLAIN,20);
	
	public Client(Socket socket, String userName) {
		try {
			this.socket=socket;
			this.userName=userName;
			this.bufferedWriter=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.printWriter=new PrintWriter(socket.getOutputStream());
		}
		catch(IOException e) {
			closeAll(socket,bufferedReader,bufferedWriter);
		}
	}
	
//	public void sendMessage() {
//		try {
//			bufferedWriter.write(userName);
//			bufferedWriter.newLine();
//			bufferedWriter.flush();
//			
//			Scanner scanner=new Scanner(System.in);
//			while(socket.isConnected()) {
//				String msg=scanner.nextLine();
//				bufferedWriter.write(userName+": "+msg);
//				bufferedWriter.newLine();
//				bufferedWriter.flush();
//			}
//		}
//		catch(IOException e) {
//			closeAll(socket,bufferedReader,bufferedWriter);
//		}
//	}
	
	private void handleEvents() {
		msgInput.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				
			}

			@Override
			public void keyPressed(KeyEvent e) {
				
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode()==10) {
					String msg=msgInput.getText();
					
					try {
						bufferedWriter.write(userName+": "+msg);
						bufferedWriter.newLine();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					msgArea.append(msg+"\n");
					printWriter.println(userName+": "+msg);
					printWriter.flush();
					msgInput.setText("");
					msgInput.requestFocus();
					
				}
				
			}
			
		});
		
	}
	public void listenForMsg() {
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				String msgFromChat;
				
				while(socket.isConnected()) {
					try {
						msgFromChat=bufferedReader.readLine();
						msgArea.append(msgFromChat+"\n");
					}
					catch(IOException e) {
						closeAll(socket,bufferedReader,bufferedWriter);
					}
				}
			}
			
		}).start();
	}
	
	private void closeAll(Socket socket2, BufferedReader bufferedReader2, BufferedWriter bufferedWriter2) {

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
	
	private void createGUI() throws IOException {
		this.setTitle("Messenger "+userName);
		this.setSize(500,600);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		heading.setFont(font);
		msgArea.setFont(font);
		msgInput.setFont(font);
		
		heading.setIcon(new ImageIcon("img.png"));
		heading.setHorizontalTextPosition(SwingConstants.CENTER);
		heading.setVerticalTextPosition(SwingConstants.BOTTOM);
		heading.setHorizontalAlignment(SwingConstants.CENTER);
		heading.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		
		msgArea.setEditable(false);
		msgInput.setHorizontalAlignment(SwingConstants.CENTER);
		
		this.setLayout(new BorderLayout());
		
		this.add(heading,BorderLayout.NORTH);
		JScrollPane jscrollpane = new JScrollPane(msgArea);
		this.add(jscrollpane,BorderLayout.CENTER);
		this.add(msgInput,BorderLayout.SOUTH);
		
		bufferedWriter.write(userName);
		bufferedWriter.newLine();
		bufferedWriter.flush();
	}
	
	public static void main(String[] args) throws IOException{
		Scanner scanner=new Scanner(System.in);
		System.out.println("Enter your UserName for group chat: ");
		String userName=scanner.nextLine();
		Socket socket=new Socket("localhost",8080);
		Client client=new Client(socket,userName);
		
		client.createGUI();
		
		client.listenForMsg();
		client.handleEvents();
		//client.sendMessage();
		
	}

}
