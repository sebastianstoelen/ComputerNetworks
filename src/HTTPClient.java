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
        String host = "www.tinyos.net";
        PrintWriter s_out = null;
        BufferedReader s_in = null;
        //String command = args[0];
        String command = "GET";
        //String URI = args[1];
        String URI = "/images/webs.jpg";
        //int port = Integer.parseInt(args[2]);
        int port = 80;
        //String version = args[3];
        String version = "1.0";
        System.out.println("Before try");
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
        String sentence;
        String totalSentence = "";
        if (command.equals("PUT")){
            BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
            do{
                sentence = inFromUser.readLine();
                totalSentence = totalSentence  + sentence + "\r\n";
            } while(sentence.length()>0);
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
        while ((response = s_in.readLine()) != null){
            System.out.println(response);
            writer.append(response);
            writer.append('\n');
        }
        writer.close();

        //send image strings to ImageHandler and create image files
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
}
