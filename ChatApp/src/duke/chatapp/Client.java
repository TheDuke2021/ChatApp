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


public class Client implements Runnable{
	
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
			printMessage("Connecting to the server...");
		} catch (InvocationTargetException | InterruptedException e) {
			System.err.println("An error occured while loading GUI.");
			System.exit(1);
		}
		
		try(Socket socket = new Socket("localhost", 80)){
			serverOutput = new PrintWriter(socket.getOutputStream(), true);
			serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			readyForInput = true;
			printMessage("Connection established! Say hello to everyone!");
			serverOutput.println(name);
			if(serverInput.read() != 1)
				throw new NameIsTakenException();
			new Thread(this).start();
			while(true) {
				String msg = serverInput.readLine();
				if(msg.equals(""))
					continue;
				printMessage(msg);
			}
		}catch(UnknownHostException e) {
			readyForInput = false;
			System.err.println("Cannot determine the IP address of the server.");
			printError("Cannot determine the IP address of the server.");
//			System.exit(1);
		}catch(IOException e) {
			readyForInput = false;
			System.err.println("An IOException occurred in the program.");
			printError("An IOException occurred in the program.");
//			System.exit(1);
		} catch (InvocationTargetException | InterruptedException e) {
			readyForInput = false;
			System.err.println("An exception in the GUI occurred while trying to update the Swing component.");
			printError("An exception in the GUI occurred while trying to update the Swing component.");
//			System.exit(1);
		}catch(NameIsTakenException e) {
			readyForInput = false;
			System.err.println("This name is already taken. Pick a different one.");
			printError("This name is already taken. Pick a different one.");
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
			serverOutput.println(msg);
			
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

	
	private void printMessage(String message) throws InvocationTargetException, InterruptedException {
		SwingUtilities.invokeAndWait(()->{
			messagesArea.setText(messagesArea.getText() + message + "\n");
			messagesArea.setCaretPosition(messagesArea.getDocument().getLength());
		});
	}
	
	private void printError(String message) {
		SwingUtilities.invokeLater(()->{
			messagesArea.setText(messagesArea.getText() + message + "\n");
			messagesArea.setCaretPosition(messagesArea.getDocument().getLength());
		});
	}
	
	public static void main(String args[]){
		
		new Client();

	}


	@Override
	public void run() {
		try {
			while(!readyForInput);
			while(true) {
				serverOutput.println("spam!");
				Thread.sleep(0, 100);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
}