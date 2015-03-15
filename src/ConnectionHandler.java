import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
/* ConnectionHandler handles every input from and output to a single client.
 * For every client a new  connectionHandler is created.
 */
public class ConnectionHandler implements Runnable {

	Locale locale = new Locale("en");
	Socket client;
	String clientSentence;
	String totalMessage;
	String command;
	String URI;
	String version;
	String code;
	String modified;
	/* Constuctor for connectionHandler.
	 * @Param socket is the socket of the client connecting.
	 */
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
				// Variable where every line the server reads is saved.
				String clientSentence;
				// Variable where evvery line read from the reader is appended to.
				String totalMessage = "";
				clientSentence = inFromClient.readLine();
				while (clientSentence.length()>0){
					if (clientSentence.contains("If-Modified-Since")){
						modified = clientSentence;
					}
					totalMessage = totalMessage  + clientSentence + "\r\n";
					clientSentence = inFromClient.readLine();
				} 
				// Method to set the different connection variables.
				getArguments(totalMessage);
				
				System.out.println("Received: " + totalMessage);
				// Checks which command was received and calls the corresponding method.
				if (command.equals("GET")){
					System.out.println(getCommand());
					outToClient.writeBytes(getCommand());
				}
				else if (command.equals("HEAD")){
					System.out.println(headCommand());
					outToClient.writeBytes(headCommand());
				}
				// Closes the outputstream.
				outToClient.close();
				// If the HTTP version is 1.0 closes the socket of the client and ends the run method thus closing the thread.
				if (version.contains("1.0")){
					client.close();
					return;
				}
            }
            catch (IOException e) {
				e.printStackTrace();
			}
			modified = null;
		}
		
	}

	private String getCommand() {
		boolean modifiedSince = false;
		String returnMessage;
		File f = new File("Server/"+URI);
		Path path = f.toPath();
		if (!f.exists()){
			code = "404 BAD REQUEST";
		}
		else {
			code = "200 OK";
			if ((modified != null)){
				Date oud = new Date(f.lastModified());
				String modifier = modified.substring(19);
				System.out.println("modifier");
				System.out.println(modifier);
				SimpleDateFormat ft = 
				      new SimpleDateFormat ("E',' dd MMM yyyy HH:mm:ss zzz",locale);
				try {
					Date check = ft.parse(modifier);
					if (!(modifiedSince = oud.compareTo(check) > 0)){
						code = "304 NOT MODIFIED";
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
				System.out.println("bool:"+ modifiedSince);
			}
		}
		
		
		returnMessage = version + " "  + code;
		returnMessage = returnMessage.replace("\r\n", "");
		String extra = extraMessage(f);
		if (!modifiedSince){
			returnMessage = returnMessage + "\r\n" + extra + "\r\n\r\n";
			return returnMessage;
		}
		else{
		try {
			returnMessage = returnMessage + "\r\n" + extra + "\r\n\r\n" + readFile(path,StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return returnMessage;}
	}
	
	private String headCommand() {
		String returnMessage;
		File f = new File("Server/"+URI);
		if(f.exists() && !f.isDirectory()) {
			code = "200 OK";
		}
		else{
			code = "404 BAD REQUEST";
		}
		returnMessage = version + " " + code;
		returnMessage = returnMessage.replace("\r\n", " ");
		String extra = extraMessage(f);
		returnMessage = returnMessage + "\r\n" + extra + "\r\n\r\n" ;
		return returnMessage;
	}

	private String extraMessage(File file) {
		Date date = new Date();
		SimpleDateFormat ft = 
			      new SimpleDateFormat ("E',' dd MMM yyyy HH:mm:ss zzz",locale);
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
		String[] arguments = totalMessage.split("\r\n")[0].split(" ");
		command = arguments[0];
		URI = arguments[1];
		if (URI.equals("/")){
			URI = "Index.html";
		}
		version = arguments[2];
		
		
	}
	
	static String readFile(Path path, Charset encoding) 
			  throws IOException 
			{
			  byte[] encoded = Files.readAllBytes(path);
			  return new String(encoded, encoding);
			}

}
