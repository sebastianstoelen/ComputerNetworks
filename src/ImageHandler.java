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
	String host;
	String URI;
	int port;
	float httpVersion;
	public ImageHandler(String clientHost, String clientURI, int clientPort, float clientHttpVersion){
		host = clientHost;
		URI = clientURI;
		port = clientPort;
		httpVersion = clientHttpVersion;
	}
	public void createImages(String[] images){
		for (String img : images){
			extractImage(img);
		}
	}

	private void extractImage(String img) {
		
		
	}

}
