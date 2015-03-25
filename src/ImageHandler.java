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


public class ImageHandler {
	String httpVersion = null;
	DataInputStream s_in = null;
	PrintWriter s_out = null;
	String host = null;
	String URI = null;
	int port;
	public ImageHandler(String clientHost, String clientURI, int clientPort, String clientHttpVersion,
						DataInputStream clientS_in, PrintWriter clientS_out){
		host = clientHost;
		URI = clientURI;
		port = clientPort;
		httpVersion = clientHttpVersion;
		s_in = clientS_in;
		s_out = clientS_out;
	}
	
	public void createImages(String[] images,DataInputStream clientS_in, PrintWriter clientS_out) throws IOException{
		if (httpVersion.contains("1.0")){
			for (String img : images){
				System.out.println(img);
				extractImage(img);
			}
		}
		else{
			extractImages(images,clientS_in, clientS_out);
		}
	}

	private void extractImages(String[] images,DataInputStream clientS_in, PrintWriter clientS_out) throws IOException {
		PrintWriter s_out = clientS_out;
		DataInputStream s_in = clientS_in;
		for (String img : images){
			String fileName = URI + img;
			File imgFile = new File(fileName);
			String message = "GET" + " " + URI +img+ " HTTP/" + httpVersion + "\r\n" +"Host: "+ host
								 + "\r\n" ;
			message= HTTPClient.addIfModifiedSince(message, imgFile);
			System.out.println("yolo");
			System.out.println(message);
			s_out.println(message);
			BufferedImage image;
			int size = 0;
			size = parseHeader(s_in,img);
	        byte[] buffer = new byte[1000];
	        int sum = 0;
	        ByteArrayOutputStream bufferSum = new ByteArrayOutputStream();
	        int amount;
	        while (sum<size){
	        	amount = s_in.read(buffer,0,1000);
	        	bufferSum.write(buffer,0,amount);
	        	sum+=amount;
	        }
	        byte[] array = bufferSum.toByteArray();
			try {
	            image = ImageIO.read(new ByteArrayInputStream(array));
	            if (image==null){
	            	System.out.println("No image received");
	            	continue;
	            }
	            
	            String[] files = fileName.split("\\.");
	            ImageIO.write(image, files[files.length-1],HTTPClient.createFile(fileName));
	 
	        } catch (IOException e) {
	        	e.printStackTrace();
	        }
		}
	}

	private void extractImage(String img) throws IOException {
		Socket s = new Socket();
		PrintWriter s_out = null;
		DataInputStream s_in = null;
		try{
            s.connect(new InetSocketAddress(host, port));
            System.out.println("Connected");

            //writer for socket
            s_out = new PrintWriter(s.getOutputStream(), true);
            //reader for socket
            s_in = new DataInputStream(s.getInputStream());
        }
		catch(UnknownHostException e){
            System.err.println("Don't know about host: " + host);
            System.exit(1);
        }
		String fileName = URI.substring(1) + img;
		File imgFile = new File(fileName);
		String message = "GET" + " " + URI +img+ " HTTP/" + httpVersion + "\r\n\r\n" ;
		message= HTTPClient.addIfModifiedSince(message, imgFile);
		System.out.println(message);
		s_out.println(message);
		BufferedImage image;
		
		int size = 0;
		size = parseHeader(s_in,img);
        byte[] buffer = new byte[1000];
        int sum = 0;
        ByteArrayOutputStream bufferSum = new ByteArrayOutputStream();
        int amount;
        while (sum<size){
        	amount = s_in.read(buffer,0,1000);
        	bufferSum.write(buffer,0,amount);
        	sum+=amount;
        }
        byte[] array = bufferSum.toByteArray();
		try {
            image = ImageIO.read(new ByteArrayInputStream(array));
            if (image==null){
            	System.out.println("No image received");
            	return;
            }
            String[] files = fileName.split("\\.");
            ImageIO.write(image, files[files.length-1],HTTPClient.createFile(fileName));
        } catch (IOException e) {
        	e.printStackTrace();
	}

	}

	private int parseHeader(DataInputStream s_in,String img) throws IOException{
		String response;
		String lastModified = null;
		int size = 0;
		while (!((response = s_in.readLine()).equals(""))){
	        System.out.println(response);
	        if (response.contains("Last-Modified:")){
	        	lastModified = response.substring(15);
	        }
	        if (response.contains("Content-Length:")){
	        	size = Integer.parseInt(response.substring(16));
	        }
	    }
	    if (lastModified != null){
	    	String fileURI = URI+img;
	    	if (fileURI.equals("/")){ // / will become /index.hmtl, otherwise the file cannot be created
	    		fileURI = "/index.html";
	    	}
	    	// Create (or overwrite) a cache file, containing the last-modified of the file.
	    	File cacheFile = new File(fileURI.substring(1,fileURI.lastIndexOf('.'))+"cache"+".txt");
	    	FileWriter cacheWriter = new FileWriter(cacheFile);
	    	cacheWriter.write(lastModified);
	    	cacheWriter.close();
	    }
		return size;
	}
	
	
}
