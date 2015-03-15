import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.text.*;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

public class ConnectionHandler implements Runnable {
	Socket client;
	String clientSentence;
	String totalMessage;
	String command;
	String URI;
	String version;
	String code;
	
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
            clientSentence = inFromClient.readLine();
            while (clientSentence.length()>0){
                totalMessage = totalMessage  + clientSentence + "\r\n";
                clientSentence = inFromClient.readLine();
                
            } ;
            getArguments(totalMessage);
            System.out.println("Received: " + totalMessage);
            if (command.equals("GET")){
            	System.out.println("binnen");
            	System.out.println(getCommand());
            	outToClient.writeBytes(getCommand());
            }
            
            String capsSentence = totalMessage.toUpperCase() + '\n';
            //outToClient.writeBytes(capsSentence);
            System.out.println("command: "+ command);}
            catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	private String getCommand() {
		String returnMessage;
		File f = new File("Server/"+URI);
		
		if(f.exists() && !f.isDirectory()) {
			code = "200 OK";
		}
		else{
			code = "404 BAD REQUEST";
		}
		returnMessage = version  + code;
		returnMessage = returnMessage.replace("\r\n", " ");
		String extra = extraMessage(f);
		returnMessage = returnMessage + "\r\n" + extra;
		return returnMessage;
	}

	private String extraMessage(File file) {
		Date date = new Date();
		Locale locale = new Locale("en");
		SimpleDateFormat ft = 
			      new SimpleDateFormat ("E',' dd MM yyyy hh:mm:ss zzz",locale);
		ft.setTimeZone(TimeZone.getTimeZone("GMT"));
		Path path = FileSystems.getDefault().getPath("Server",URI);
		String type = "";
		try {
			type = Files.probeContentType(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Date: " + ft.format(date) + "\r\n" + "Content-Type: " + type + "\r\n" + "Content-Length: " + file.length();
	}

	private void getArguments(String totalMessage) {
		String[] arguments = totalMessage.split(" ");
		command = arguments[0];
		URI = arguments[1];
		if (URI.equals("/")){
			URI = "Index.html";
		}
		System.out.println("uri:"+ URI);
		version = arguments[2];
		
	}

}
