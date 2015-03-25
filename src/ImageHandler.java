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
	public ImageHandler(String clientHost, String clientURI, int clientPort, String clientHttpVersion,
						DataInputStream clientS_in, PrintWriter clientS_out){
		String host = clientHost;
		String URI = clientURI;
		int port = clientPort;
		String httpVersion = clientHttpVersion;
		DataInputStream s_in = clientS_in;
		PrintWriter s_out = clientS_out;
	}
	
	public static void main(String[] args) throws IOException{
		ImageHandler handler = new ImageHandler("www.linux-ip.net","/images/logo-title.jpg",80, "1.0");
		handler.extractImage("");
	}
	public void createImages(String[] images) throws IOException{
		if (httpVersion.contains("1.0")){
			for (String img : images){
				System.out.println(img);
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
			String message = "GET" + " " + URI +img+ " HTTP/" + httpVersion + "\r\n" +"Host: "+ host+ "\r\n" 
								+ "Connection: keep-alive" + "\r\n\r\n";
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
	        System.out.println("\r\n");
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
	            String fileName = img;
	            fileName = fileName.replace('/','-');
	            String [] files = fileName.split("\\.");
	            ImageIO.write(image, files[1],new File(fileName));
	 
	        } catch (IOException e) {
	        	e.printStackTrace();
	        }
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
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
        System.out.println("\r\n");
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
            String fileName = URI + img;
            String[] files = fileName.split("\\.");
            ImageIO.write(image, files[files.length-1],HTTPClient.createFile(fileName));
 
        } catch (IOException e) {
        	e.printStackTrace();
	}

	}
	
	
}
