import java.io.*;
import java.net.*;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

public class ConnectionHandler implements Runnable {
	Socket client;
	String clientSentence;
	String totalMessage;
	String command;
	String URI;
	String version;
	
	public ConnectionHandler(Socket socket){
		client = socket;
	}

	@Override
	public void run(){
		while(true) {
			// Create inputstream (convenient data reader) to this host.
            BufferedReader inFromClient;
			try {
				inFromClient = new BufferedReader(new InputStreamReader	(client.getInputStream()));
            // Create outputstream (convenient data writer) to this host.
            DataOutputStream outToClient = new DataOutputStream(client.getOutputStream());
            String clientSentence;
            String totalMessage = "";
            // Read text from the client, make it uppercase and write it back.
            do{
                clientSentence = inFromClient.readLine();
                totalMessage = totalMessage  + clientSentence + "\r\n";
                
            } while (clientSentence.length()>1);
            getArguments(totalMessage);
            System.out.println("Received: " + totalMessage);
            
            String capsSentence = totalMessage.toUpperCase() + '\n';
            outToClient.writeBytes(capsSentence);
            System.out.println("command: "+ command);}
            catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	private void getArguments(String totalMessage) {
		String[] arguments = totalMessage.split(" ");
		command = arguments[0];
		URI = arguments[1];
		version = arguments[2];
		
	}

}
