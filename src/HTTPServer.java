
import java.io.*;
import java.net.*;


class HTTPServer
{

    
    public static void main(String argv[]) throws Exception
    {
        // Create server (incoming) socket on port 6789.
        ServerSocket welcomeSocket = new ServerSocket(6789);
        
        // Wait for a connection to be made to the server socket.
        while(true)
        {
            // Create	 a 'real' socket  and a thread from the Server socket.
            Socket connectionSocket = welcomeSocket.accept();
            Runnable connectionHandler = new ConnectionHandler(connectionSocket);
            // Call connectionHandler to andle the incoming and outgoing data from socket.
            new Thread(connectionHandler).start();

        } 

    } // End of main method.

} // End of class TCPServer
