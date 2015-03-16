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

/**
 * Created by Sebastian Stoelen on 04/03/15.
 */
public class HTTPClient {

    public static void main(String[] args) throws IOException{
        Socket s = new Socket();
        PrintWriter s_out = null;
        BufferedReader s_in = null;
        String command = args[0];
        int port = Integer.parseInt(args[2]);
        String version = args[3];
        String host;
        String URI;
        if (version.equals("1.0")){
            host = extractHost(args[1])[0];
            URI = extractHost(args[1])[1];
        }
        else {
        	BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        	System.out.print("Host: ");
        	host = inFromUser.readLine();
        	URI = args[1];
        }
        try{
            s.connect(new InetSocketAddress(host, port));
            System.out.println("Connected");

            //writer for socket
            s_out = new PrintWriter(s.getOutputStream(), true);
            //reader for socket
            s_in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        }
        //Host not found

        catch(UnknownHostException e){
            System.err.println("Don't know about host: " + host);
            System.exit(1);
        }
        while(true){
        	String totalSentence = "";
	        if (command.equals("PUT")){
	        	totalSentence = putCommand();
	        }
	        //Send message to server
	        String message = command + " " + URI + " HTTP/" + version +"\r\n" + totalSentence + "\r\n\r\n" ;
	        System.out.println(message);
	        s_out.println(message); 
	        String filename = command + "-" + URI.substring(1);
	        filename = filename.replace("/","-");
	        System.out.println(filename);
	        FileWriter writer = new FileWriter(filename);
	        
	        //Get response from server
	        String response;
	        while (!((response = s_in.readLine()) == null) && s_in.ready()){
	            System.out.println(response);
	            writer.append(response);
	            writer.append("\r\n");
	        }
	        System.out.println("UUUUUUUIIIIT");
	        writer.close();
	        
	        if (command.equals("GET")){
	        	getCommand(filename, host, URI, port, version);
	        }
	        if (version.contains("1.0")){
	        	s.close();
	        	return;
	        }
        	System.out.println("Enter new HTTP command. Type 'exit' to escape.");
        	BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
        	String commandSentence = inFromUser.readLine();
        	System.out.println("YOOOO");
        	if (commandSentence != null){
        		if (commandSentence.toLowerCase().equals("exit")){
        			return;
        		}
        		String[] arguments = commandSentence.split(" ");
        		command = arguments[0];
        		URI = arguments[1];
        		version = arguments[2].substring(5);
	        }
        }
    }
    
    private static String putCommand() throws IOException{
    	String sentence = "";
    	String totalSentence = "";
    	BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
        do{
            sentence = inFromUser.readLine();
            totalSentence = totalSentence  + sentence + "\r\n";
        } while(sentence.length()>0);
        inFromUser.close();
        return totalSentence;
    }
      
    private static void getCommand(String filename, String host, String URI, int port, String version) 
    		throws IOException{
    	File input = new File(filename);
        Document doc = Jsoup.parse(input, "UTF-8", host);
        Elements img = doc.getElementsByTag("img");
        String[] srcImages = new String[img.size()];
        int counter = 0;
        for (Element el : img){
            String srcTag = el.attr("src");
            srcImages[counter] = srcTag;
            System.out.println("image tag: " + el.attr("src"));
            counter += 1;
        }
        ImageHandler imageHandler = new ImageHandler(host, URI, port, Float.parseFloat(version));
        imageHandler.createImages(srcImages);
    }
    
    private static String[] extractHost(String fullAdress){
    	int index = fullAdress.indexOf('/');
    	String host = fullAdress.substring(0, index);
    	String URI = fullAdress.substring(index);
    	String[] re = {host, URI};
    	return re;
    }
}
