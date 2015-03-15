//Import necessary IO and NETwork libraries
import java.io.*;
import java.net.*;

/*
 * A simple example TCP Server application
 *
 * Computer Networks, KU Leuven.
 *
 */
class HTTPServer
{

    /*
     * Everything is included in the main method.
     */
    public static void main(String argv[]) throws Exception
    {
        // Create server (incoming) socket on port 6789.
        ServerSocket welcomeSocket = new ServerSocket(6789);
        
        // Wait for a connection to be made to the server socket.
        while(true)
        {
            // Create	 a 'real' socket from the Server socket.
            Socket connectionSocket = welcomeSocket.accept();
            Runnable connectionHandler = new ConnectionHandler(connectionSocket);
            new Thread(connectionHandler).start();

        } 

    } // End of main method.

} // End of class TCPServer
