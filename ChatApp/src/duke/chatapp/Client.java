package duke.chatapp;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;


public class Client{
	
	private String name = "";
	private PrintWriter serverOutput;
	private BufferedReader serverInput;
	
	private JFrame frame;
	private JTextArea messagesArea;
	private JScrollPane scrollPane;
	private JTextField messageField;
	private boolean readyForInput;
	
	
	Client(){
		
		try {
			SwingUtilities.invokeAndWait(()-> loadGUI());
		} catch (InvocationTargetException | InterruptedException e) {
			System.err.println("An error occured while loading GUI.");
			System.exit(1);
		}
		
		messagesArea.setText("Connecting to the server...");
		try(Socket socket = new Socket("localhost", 80)){
			serverOutput = new PrintWriter(socket.getOutputStream(), true);
			serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			readyForInput = true;
			messagesArea.setText(messagesArea.getText() + "\nConnection established! Say hello to everyone!");
			while(true) {
				String msg = serverInput.readLine();
				if(msg.equals(""))
					continue;
				//TODO You probably should use EventListener instead of changing messagesArea attributes directly from this thread.
				System.out.println(SwingUtilities.isEventDispatchThread());
				messagesArea.setText(messagesArea.getText() + "\n" + msg);
				messagesArea.setCaretPosition(messagesArea.getDocument().getLength());
			}
		}catch(UnknownHostException e) {
			readyForInput = false;
			System.err.println("Cannot determine the IP address of the server.");
			messagesArea.setText(messagesArea.getText() + "\nCannot determine the IP address of the server.");
//			System.exit(1);
		}catch(IOException e) {
			readyForInput = false;
			System.err.println("An IOException occured in the program.");
			messagesArea.setText(messagesArea.getText() + "\nAn IOException occured in the program.");
//			System.exit(1);
		}
	}
	
	
	private void loadGUI() {

		frame = new JFrame("Chat Application");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600, 400);
		frame.setResizable(false);
		frame.setAlwaysOnTop(true);
		frame.setLocationRelativeTo(null);
		
		
		messagesArea = new JTextArea();
		messagesArea.setEditable(false);
		messagesArea.setLineWrap(true);
		messagesArea.setFont(new Font("Times New Roman", Font.PLAIN, 16));
		
		messageField = new JTextField();
		messageField.setFont(new Font("Times New Roman", Font.PLAIN, 16));
		
		//Here the user's input is caught
		messageField.addActionListener((e) -> {
			if(!readyForInput)
				return;
			String msg = messageField.getText().trim();
			messageField.setText("");
			if(msg.equals(""))
				return;
			serverOutput.println(name + "\n" + msg);
			
		});
		
		scrollPane = new JScrollPane(messagesArea);
		
		
		frame.add(scrollPane, BorderLayout.CENTER);
		frame.add(messageField, BorderLayout.SOUTH);
		
		//Here the user types in their name
		while(name.equals("")) {
			name = JOptionPane.showInputDialog("Input your name:");
			if(name == null)
				System.exit(0);
			name = name.trim();
		}
		
		frame.setTitle("Chat Application [" + name + "]");
		
		frame.addWindowListener( new WindowAdapter() {
		    public void windowOpened( WindowEvent e ){
		        messageField.requestFocus();
		    }
		}); 
		frame.setVisible(true);
	}

	
	public static void main(String args[]){
		
		new Client();

	}
}