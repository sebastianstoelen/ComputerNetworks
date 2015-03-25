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
				// Variable where every line the server reads is saved.
				String clientSentence;
				// Variable where every line read from the reader is appended to.
				String totalMessage = "";
				Date date = new Date();
				long tijd1 = date.getTime();
				while(inFromClient.available()==0){
					long tijd2 =new Date().getTime();
					if ((tijd2-tijd1)>60000){
						outToClient.close();
						client.close();
						System.out.println("Tis gedaan");
						return;
					}
				}
					
				while (!((clientSentence= inFromClient.readLine()).equals(""))){
					System.out.println(clientSentence);
					if (clientSentence.contains("If-Modified-Since")){
						modified = clientSentence;
					}
					if (clientSentence.contains("Content-Length:")){
		            	size = Integer.parseInt(clientSentence.substring(16));
		            }
					
					totalMessage = totalMessage  + clientSentence + "\r\n";
				} 
				// Method to set the different connection variables.
				getArguments(totalMessage);
				System.out.println("Received: " + totalMessage);
				// Checks which command was received and calls the corresponding method.
				if (command.equals("GET")){
					System.out.println("GET");
					System.out.println(getCommand());
					outToClient.writeBytes(getCommand());
				}
				else if (command.equals("HEAD")){
					System.out.println(headCommand());
					outToClient.writeBytes(headCommand());
				}
				else if (command.equals("PUT")){
					outToClient.writeBytes(putCommand(inFromClient,size));
				}
				
				else if (command.equals("POST")){
					outToClient.writeBytes(postCommand(inFromClient,size));
				}
				
				
				
				// Closes the outputstream.
				// If the HTTP version is 1.0 closes the socket of the client and ends the run method thus closing the thread.
				if (version.contains("1.0")){
					outToClient.close();
					client.close();
					System.out.println("Tis gedaan");
					return;
				}
            }
            catch (IOException e) {
				e.printStackTrace();
			}
			modified = null;
		}
		
	}

	private String postCommand(DataInputStream inFromClient,int size) {
		boolean modifiedSince = false;
		String returnMessage;
		File f = new File("Server/"+URI);
		if (f.isDirectory()){
			code = "404 BAD REQUEST";
		}
		else {
			code = "200 OK";
			if ((modified != null && f.exists())){
				Date oud = new Date(f.lastModified());
				String modifier = modified.substring(19);
				SimpleDateFormat ft = 
				      new SimpleDateFormat ("E',' dd MMM yyyy HH:mm:ss zzz",locale);
				try {
					Date check = ft.parse(modifier);
					if ((modifiedSince = oud.compareTo(check) > 0)){
						code = "304 NOT MODIFIED";
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
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
		return returnMessage;}
	}

	private String getCommand() {
		boolean modifiedSince = true;
		String returnMessage;
		File f = new File("Server/"+URI);
		Path path = f.toPath();
		SimpleDateFormat ft = 
			      new SimpleDateFormat ("E',' dd MMM yyyy HH:mm:ss zzz",locale);
		ft.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date oud = new Date(f.lastModified());
		if (!f.exists()){
			code = "404 BAD REQUEST";
		}
		else {
			code = "200 OK";
			if ((modified != null)){
				String modifier = modified.substring(19);
				try {
					Date check = ft.parse(modifier);
					if (!(modifiedSince = oud.compareTo(check) > 0)){
						code = "304 NOT MODIFIED";
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
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
	
	private String putCommand(DataInputStream inFromClient,int size) {
		boolean modifiedSince = false;
		String returnMessage;
		File f = new File("Server/"+URI);
		if (f.isDirectory()){
			code = "404 BAD REQUEST";
		}
		else {
			code = "200 OK";
			if ((modified != null && f.exists())){
				Date oud = new Date(f.lastModified());
				String modifier = modified.substring(19);
				SimpleDateFormat ft = 
				      new SimpleDateFormat ("E',' dd MMM yyyy HH:mm:ss zzz",locale);
				try {
					Date check = ft.parse(modifier);
					if ((modifiedSince = oud.compareTo(check) > 0)){
						code = "304 NOT MODIFIED";
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
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
	
	private String headCommand() {
		String returnMessage;
		File f = new File("Server/"+URI);
		if (!f.exists()){
			code = "404 BAD REQUEST";
		}
		else {
			code = "200 OK";
			if ((modified != null)){
				Date oud = new Date(f.lastModified());
				String modifier = modified.substring(19);
				SimpleDateFormat ft = 
				      new SimpleDateFormat ("E',' dd MMM yyyy HH:mm:ss zzz",locale);
				try {
					Date check = ft.parse(modifier);
					if (!(oud.compareTo(check) > 0)){
						code = "304 NOT MODIFIED";
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
		returnMessage = version + " " + code;
		returnMessage = returnMessage.replace("\r\n", "");
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
		return "Date: " + ft.format(date) + "\r\n" + "Content-Type: " + type + "\r\n" + "Content-Length: " + file.length() + "\r\n"+ "Last-Modified: " + ft.format(file.lastModified());
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
	
	
	
	static String readFile(Path path, Charset encoding) 
			  throws IOException 
			{
			  byte[] encoded = Files.readAllBytes(path);
			  return new String(encoded, encoding);
			}

}
