import java.io.*;
import java.net.*;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

/**
 * Created by Sebastian Stoelen on 04/03/15.
 */
public class HTTPClient {

    public static void main(String[] args) throws IOException{
        Socket s = new Socket();
        PrintWriter s_out = null;
        DataInputStream s_in = null;
        String command = args[0];
        int port = Integer.parseInt(args[2]);
        String version = args[3];
        String host;
        String URI;
        String totalSentence = ""; //the information to be appended after the command (e.g. Host: ...)
        if (version.equals("1.0")){
            host = splitFullAddress(args[1])[0];
            URI = splitFullAddress(args[1])[1];
        }
        else {
        	BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        	System.out.print("Host: ");
        	host = inFromUser.readLine();
        	URI = args[1];
        	totalSentence = ("Host: " + host);
        	System.out.print("Connection: ");
        	String connection = inFromUser.readLine();
        	if (connection.equals("")){
        		connection = "keep-alive";
        	}
        	totalSentence = totalSentence + ("\r\n" + "Connection: " + connection);
        	System.out.println("Total: " + totalSentence);

        }
        //connect to a new socket, given the host.
        try{
            s.connect(new InetSocketAddress(host, port));
            System.out.println("Connected");

            //writer for socket
            s_out = new PrintWriter(s.getOutputStream(), true);
            //reader for socket
            s_in = new DataInputStream(s.getInputStream());
        }
        //Throw an error if the host is nod found
        catch(UnknownHostException e){
            System.err.println("Don't know about host: " + host);
            System.exit(1);
        }
        while(true){
        	// Construct the total message if the user issues a 'PUT' command.
	        if (command.equals("PUT") || command.equals("POST")){
	        	totalSentence = putOrPostCommand(totalSentence);
	        }
	        //Send message to server
	        String message = command + " " + URI + " HTTP/" + version +"\r\n" + totalSentence ;
	        File filename = createFile(URI);
	        if ((command.equals("GET") || command.equals("HEAD")) && filename.exists()) {
	        	message = addIfModifiedSince(message, filename);
	        }
	        System.out.println(message);
	        s_out.println(message);
	        System.out.println(filename);
	        FileWriter writer = new FileWriter(filename, true);
	        
	        //Get response from server
	        String response;
	        String lastModified = null;
	        int size =0;
	        while (!((response = s_in.readLine()).equals(""))){ //Read the header information the server has sent back.
	            System.out.println(response);
	            if (response.contains("Content-Length:")){
	            	size = Integer.parseInt(response.substring(16));
	            }
	            if (response.contains("Last-Modified:")){
	            	lastModified = response.substring(15);
	            }
	        }
	        //retrieve all the embedded images if the user issued a 'GET' command
	        if (command.equals("GET")){
	        	getCommand(filename, host, URI, port, version, writer, size, s_in);
	        	
	        // set the last-modified of a file, if the server has returned this information.
	        if (lastModified != null){
	        	String fileURI = URI;
	        	if (fileURI.equals("/")){ // / will become /index.hmtl, otherwise the file cannot be created
	        		fileURI = "/index.html";
	        	}
	        	// Create (or overwrite) a cache file, containing the last-modified of the file.
	        	File cacheFile = new File(fileURI.substring(1,fileURI.lastIndexOf('.'))+"cache"+".txt");
	        	FileWriter cacheWriter = new FileWriter(cacheFile);
	        	cacheWriter.write(lastModified);
	        	cacheWriter.close();
	        }
	        
	        }
	        //exit after one command if the version of HTTP is 1.0 
	        if (version.contains("1.0")){
	        	s.close();
	        	return;
	        }
        	// If the version of HTTP is 1.1, the client will prompt the user to give a new HTTP command. 
        	// The necessary parameters will be changed accordingly.
        	System.out.println("Enter new HTTP command. Type 'exit' to escape.");
        	BufferedReader inFromUser1 = new BufferedReader( new InputStreamReader(System.in));
        	while (! inFromUser1.ready()){ //check if socket is still connected while the user is not done typing
        		if (! s.isConnected()){
        			return;
        		}
        	}
        	String commandSentence = inFromUser1.readLine();
        	if (commandSentence != null){
        		if (commandSentence.toLowerCase().equals("exit")){
        			return;
        		}
        		String[] arguments = commandSentence.split(" ");
        		command = arguments[0];
        		URI = arguments[1];
        		version = arguments[2].substring(5);
	        }
        	System.out.print("Host: ");
        	totalSentence = inFromUser1.readLine();
        }
    }
    
    /*
     * Method to construct the full message when the user executes a 'PUT' command.
     * 
     * @param currentSentence | The starting String to which the user input should be appended (this String can be empty).
     * @returns totalSentece | The total message, the starting currentSentence appended with the user's input.
     */
    private static String putOrPostCommand(String currentSentence) throws IOException{
    	String sentence = "";
    	String totalSentence = currentSentence;
    	BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
    	System.out.print("Content-Length: ");
    	totalSentence = totalSentence  + "Content-Length: " + inFromUser.readLine()+"\r\n";
        while (!(sentence= inFromUser.readLine()).equals("exit")){
            totalSentence = totalSentence +"\r\n" + sentence ;
        }
        inFromUser.close();
        return totalSentence;
    }
    
    /*
     * Method to write the retreived file to disc and extract all the images embedded in the given URI.
     * 
     * @param input | The file from which the <img> tags should be fetched.
     * @param host | The host server.
     * @param URI | The URI of the input file.
     * @param port | The port via which the client is connected to the server.
     * @param version | The HTTP version.
     * @param writer | the filewriter used to write to the right file
     * @param size | the size of the file in bytes
     * @param s_in | the dataInputStream to read the bytes from
     * 
     * @effect 	All images found in the input file will be created and stored at the right locations, i.e. in the right directories.
     * 			If some images can't be fetched, for whatever reason, this image will be skipped.
     */
    private static void getCommand(File input, String host, String URI, int port, String version, 
    		FileWriter writer, int size, DataInputStream s_in) 
    		throws IOException{
    	writer.append("\r\n");
        byte[] buffer = new byte[1000];
        int sum = 0;
        ByteArrayOutputStream bufferSum = new ByteArrayOutputStream();
        int amount;
        while (sum<size){ //read the actual file from the server
        	amount = s_in.read(buffer,0,1000);
        	bufferSum.write(buffer,0,amount);
        	sum+=amount;
        }
        System.out.println("\n");
        System.out.println(bufferSum.toString("UTF-8"));
        writer.write(bufferSum.toString("UTF-8").replaceAll("\n", "\r\n"));
        writer.close();
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
        URI = URI.substring(0, URI.lastIndexOf('/') + 1);
        ImageHandler imageHandler = new ImageHandler(host, URI, port, version);
        imageHandler.createImages(srcImages);
    }
    
    /*
     * Method to split the given fullAddress in a host and a URI.
     * 
     * @param fullAddress | The full address 
     * @returns | String[] splitAddress = {fullAddress.substring(0,fullAddress.indexOf('/),
     * 													fullAddres.substring(fullAddress.indexOf('/')}
     */
    private static String[] splitFullAddress(String fullAddress){
    	int index = fullAddress.indexOf('/');
    	String host = fullAddress.substring(0, index);
    	String URI = fullAddress.substring(index);
    	String[] splitAddress = {host, URI};
    	return splitAddress;
    }
    
    /*
     * Method to create all necessary directories to create the file to be fetched from a given host. This file will
     * be created inside the (if any) created directories.
     * 
     * @param URI | The URI of the file to be created.
     * @returns newFile | The newly created File.
     */
    protected static File createFile(String URI) {
    	int cutIndex = URI.lastIndexOf('/');
    	String directories = URI.substring(0,cutIndex); //get the path to the file
    	if (directories.length() > 1){
    		File dirs = new File(directories.substring(1)); //make the necessary directories
    		Boolean result = dirs.mkdirs();
    		System.out.println("Created directories? " + result.toString());
    	}
    	if (URI.equals("/")){ // / will become /index.hmtl, otherwise the file cannot be created
    		URI = "/index.html";
    	}
    	File newFile = new File(URI.substring(1));
    	return newFile;
    }
    
    /*
     * Method to add the If-Modified-Since header, if the file has a chache where the Modified-Since information is stored.
     * 
     * @param message | The previous message that should be sent to the server
     * @param filename | The file from which the cache should be checked.
     */
    private static String addIfModifiedSince(String message, File filename) throws IOException{
    	File cacheFile = new File(getCacheFromFile(filename));
    	if ( cacheFile.exists()){
    		BufferedReader br = new BufferedReader(new FileReader(cacheFile));
    		String ifModifiedSince = br.readLine();
    		br.close();
    		if (ifModifiedSince != null){
    			message = message.split("\r\n\r\n")[0];
    			System.out.println(message);
    			message = message + "\r\n" + "If-Modified-Since: " + ifModifiedSince + "\r\n\r\n";
    		}
    	}
    	return message;
    }
    
    /*
     * Method to get the name of the cache of the given file
     * 
     * @param filename | The file of which the cache should be found.
     * @returns path.substring(0,path.lastIndexOf('.'))+"cache"+".txt"
     */
    private static String getCacheFromFile(File filename){
    	String path = filename.getPath();
    	return path.substring(0,path.lastIndexOf('.'))+"cache"+".txt";
    }
}
