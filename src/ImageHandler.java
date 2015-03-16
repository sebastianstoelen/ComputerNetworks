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
	String host = "www.linux-ip.net";
	String URI = "/images/logo-title.jpg";
	int port = 80;
	double httpVersion = 1.0;
	DataInputStream Z;
	public ImageHandler(String clientHost, String clientURI, int clientPort, double clientHttpVersion){
		host = clientHost;
		URI = clientURI;
		port = clientPort;
		httpVersion = clientHttpVersion;
	}
	
	public static void main(String[] args) throws IOException{
		ImageHandler handler = new ImageHandler("www.linux-ip.net","/images/logo-title.jpg",80, 1.0);
		handler.extractImage("");
	}
	public void createImages(String[] images) throws IOException{
		if (httpVersion == 1.0){
			for (String img : images){
				extractImage(img);
			}
		}
		else{
			extractImages(images);
		}
	}

	private void extractImages(String[] images) throws IOException {
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
		for (String img : images){
			String message = "GET" + " " + URI +img+ " HTTP/" + httpVersion + "\r\n\r\n" ;
			System.out.println(message);
			s_out.println(message);
			BufferedImage image;
			String response;
			int size = 0;
	        while (!((response = s_in.readLine()).equals(""))){
	            System.out.println(response);
	            if (response.contains("Content-Length:")){
	            	size = Integer.parseInt(response.substring(16));
	            }
	        }
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
	            String fileName = img;
	            fileName = fileName.replace('/','-');
	            String [] files = fileName.split("\\.");
	            ImageIO.write(image, files[1],new File(fileName));
	 
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
		
		String message = "GET" + " " + URI +img+ " HTTP/" + httpVersion + "\r\n\r\n" ;
		System.out.println(message);
		s_out.println(message);
		BufferedImage image;
		String response;
		int size = 0;
        while (!((response = s_in.readLine()).equals(""))){
            System.out.println(response);
            if (response.contains("Content-Length:")){
            	size = Integer.parseInt(response.substring(16));
            }
        }
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
            String fileName = img;
            fileName = fileName.replace('/','-');
            String [] files = fileName.split("\\.");
            ImageIO.write(image, files[1],new File(fileName));
 
        } catch (IOException e) {
        	e.printStackTrace();
	}

	}
	
}
