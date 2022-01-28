package com.company;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static final int SERVER_PORT = 8000;
    private static final String IP_ADDRESS = "localhost";

    public static void main(String[] args) {

        DataOutputStream toServer;
        DataInputStream fromServer;
        Scanner input = new Scanner(System.in);
        String message;

        //attempt to connect to the server
        try {
            Socket socket = new Socket(IP_ADDRESS, SERVER_PORT);

            //create input stream to receive data
            //from the server
            fromServer = new DataInputStream(socket.getInputStream());

            toServer = new DataOutputStream(socket.getOutputStream());

            //Need to have a break later boolean to allow the program to know wether to break later or not.
            //Issue is caused by the fact that message is update when the server returns a response, so it must be
            //checked what the client is sending before and set a boolean to break the loop later.
            boolean breakLater = false;

            while(true) {
                System.out.print("C: ");
                message = input.nextLine();
                toServer.writeUTF(message);
                if(message.equalsIgnoreCase("LOGOUT") || message.equalsIgnoreCase("SHUTDOWN")) {
                    breakLater = true;
                }

                //received message:
                message = fromServer.readUTF();
                System.out.println("S: " + message);
                if(breakLater){
                    break;
                }

            }

        }
        catch(IOException ex) {
            ex.printStackTrace();
        }//end try-catch


    }//end main
}

