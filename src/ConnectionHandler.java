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

	static Locale locale = new Locale("en");
	Socket client;
	String clientSentence;
	String totalMessage;
	String command;
	String URI;
	String version;
	String code;
	String modified;
	String connection = "keep-alive";
	String host = null;
	int size;
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
            DataInputStream inFromClient;
			try {
				inFromClient = new DataInputStream(client.getInputStream());
				// Create outputstream (convenient data writer) to this host.
				DataOutputStream outToClient = new DataOutputStream(client.getOutputStream());
				// Variable where every line read from the reader is appended to.
				String totalMessage = "";
				Date date = new Date();
				long tijd1 = date.getTime();
				while(inFromClient.available()==0){
					long tijd2 =new Date().getTime();
					if ((tijd2-tijd1)>60000){
						outToClient.close();
						client.close();
						return;
					}
				}
					
				while (!((clientSentence= inFromClient.readLine()).equals(""))){
					System.out.println(clientSentence);
					if (clientSentence.contains("Host")){
						host = clientSentence.substring(6);
					}
					if (clientSentence.contains("If-Modified-Since")){
						modified = clientSentence.substring(19);
					}
					if (clientSentence.contains("Content-Length:")){
		            	size = Integer.parseInt(clientSentence.substring(16));
		            }
					if (clientSentence.contains("Connection:")){
						connection = clientSentence.substring(12);
					}
					
					totalMessage = totalMessage  + clientSentence + "\r\n";
				} 
				getArguments(totalMessage);
				System.out.println("hosit"+ host);
				if ( (version.contains("1.1")) & (host==null||!host.equals("localhost")) ){
					outToClient.writeBytes(version + " 400 BAD REQUEST" + "\r\n" + extraMessage(null));
					client.close();
					return;
				}
				// Method to set the different connection variables.
				System.out.println("Received: " + totalMessage);
				// Checks which command was received and calls the corresponding method.
				File f = new File("Server/"+URI);
				if (isModifiedSince(f)){
					if (command.equals("GET")){
						outToClient.writeBytes(getCommand(f));
					}
					else if (command.equals("HEAD")){
						outToClient.writeBytes(headCommand(f));
					}
					else if (command.equals("PUT")){
						outToClient.writeBytes(putCommand(f,inFromClient,size));
					}
					
					else if (command.equals("POST")){
						outToClient.writeBytes(postCommand(f,inFromClient,size));
					}
					
					else {
						code = "400 BAD REQUEST";
						outToClient.writeBytes(version + " " + code + extraMessage(null));
					}
					
					// Closes the outputstream.
					// If the HTTP version is 1.0 closes the socket of the client and ends the run method thus closing the thread.
					if (version.contains("1.0") || connection.equals("close")){
						outToClient.close();
						client.close();
						return;
					}
				}
				else {
					String returnMessage = version + " " + "304 NOT MODIFIED";
					outToClient.writeBytes(returnMessage + extraMessage(null));
				}
            }
            catch (IOException e) {
				e.printStackTrace();
			}
			modified = null;
			host = null;
		}
		
	}

	private String postCommand(File f,DataInputStream inFromClient,int size) {
		boolean modifiedSince = false;
		String returnMessage;
		if (f.isDirectory()){
			code = "400 BAD REQUEST";
		}
		else {
			code = "200 OK";
		}
		returnMessage = version + " "  + code;
		returnMessage = returnMessage.replace("\r\n", "");
		String extra = extraMessage(f);
		if (modifiedSince){
			returnMessage = returnMessage + "\r\n" + extra + "\r\n\r\n";
			return returnMessage;
		}
		else{
			try {
				FileWriter writer = new FileWriter(f,true);
				writeMessage(inFromClient,size,writer);
				extra = extraMessage(f);
				returnMessage = returnMessage + "\r\n" + extra + "\r\n\r\n";
			} catch (IOException e) {
				e.printStackTrace();
			}
		return returnMessage;
		}
	}

	private String getCommand(File f) {
		boolean modifiedSince = true;
		String returnMessage;
		Path path = f.toPath();
		if (!f.exists()){
			code = "404 NOT FOUND";
		}
		else {
			code = "200 OK";
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
	
	private String putCommand(File f,DataInputStream inFromClient,int size) {
		boolean modifiedSince = false;
		String returnMessage;
		if (f.isDirectory()){
			code = "400 BAD REQUEST";
		}
		else {
			code = "200 OK";
		}
		returnMessage = version + " "  + code;
		returnMessage = returnMessage.replace("\r\n", "");
		String extra = extraMessage(f);
		if (modifiedSince){
			returnMessage = returnMessage + "\r\n" + extra + "\r\n\r\n";
			return returnMessage;
		}
		else{
			try {
				FileWriter writer = new FileWriter(f,false);
				writeMessage(inFromClient,size,writer);
				extra = extraMessage(f);
				returnMessage = returnMessage + "\r\n" + extra + "\r\n\r\n";
			} catch (IOException e) {
				e.printStackTrace();
			}
		return returnMessage;}
	}
	
	private String headCommand(File f) {
		String returnMessage;
		if (!f.exists()){
			code = "404 NOT FOUND";
		}
		else {
			code = "200 OK";
		}
		returnMessage = version + " " + code;
		returnMessage = returnMessage.replace("\r\n", "");
		String extra = extraMessage(f);
		returnMessage = returnMessage + "\r\n" + extra + "\r\n\r\n" ;
		return returnMessage;
			
	}
	
	private String extraMessage(File file) {
		String returnMessage;
		Date date = new Date();
		SimpleDateFormat ft = 
			      new SimpleDateFormat ("E',' dd MMM yyyy HH:mm:ss zzz",locale);
		ft.setTimeZone(TimeZone.getTimeZone("GMT"));
		returnMessage = "Date: " + ft.format(date) + "\r\n";
		if (file != null){
			Path path = file.toPath();
			String type = "";
			try {
				
				type = Files.probeContentType(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
			returnMessage = returnMessage + "Content-Type: " + type + "\r\n" + "Content-Length: " + file.length() + "\r\n"+ "Last-Modified: " + ft.format(file.lastModified());
		}
		return returnMessage;
	}
	private void getArguments(String totalMessage) {
		String[] arguments = totalMessage.split("\r\n")[0].split(" ");
		command = arguments[0];
		URI = arguments[1];
		if (URI.equals("/")){
			URI = "/index.html";
		}
		version = arguments[2];
	}
	
	private void writeMessage(DataInputStream inFromClient, int size,FileWriter writer) throws IOException{
		byte[] buffer = new byte[1000];
        int sum = 0;
        ByteArrayOutputStream bufferSum = new ByteArrayOutputStream();
        int amount;
        while (sum<size){ //read the actual file from the server
        	amount = inFromClient.read(buffer,0,1000);
        	bufferSum.write(buffer,0,amount);
        	sum+=amount;
        }
        String bufferString = bufferSum.toString();
        bufferString = bufferString.substring(0, bufferString.length()-2);
        writer.write(bufferString);
        writer.close();
	}
	
	private boolean isModifiedSince(File f){
		boolean isModified = true;
		if (modified != null){
			Date oldDate = new Date(f.lastModified());
			SimpleDateFormat ft = 
				      new SimpleDateFormat ("E',' dd MMM yyyy HH:mm:ss zzz",locale);
			try {
				Date check = ft.parse(modified);
				if (!(oldDate.compareTo(check) > 0)){
					isModified = false;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return isModified;
	}
	

	static String readFile(Path path, Charset encoding) 
			  throws IOException 
			{
			  byte[] encoded = Files.readAllBytes(path);
			  return new String(encoded, encoding);
			}

}
