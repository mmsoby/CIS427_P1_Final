
package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;


public class Server {

    private static final int SERVER_PORT = 8000;
    private static final String USERS_FILENAME = "logins.txt";
    private static ArrayList<String> logins = new ArrayList<>();
    private static boolean isLoggedIn = false;
    private static String currentUser = "";

    public static void main(String[] args) {
        loadUsers();
        boolean result;
        do{
            result = createCommunicationLoop();
        } while(result);
    }//end main

    public static void loadUsers(){
        try {
            Scanner sc = new Scanner(new File(USERS_FILENAME));
            sc.useDelimiter("\n");   //sets the delimiter pattern
            while (sc.hasNext()){
                logins.add(sc.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static boolean createCommunicationLoop() {
        try {
            //create server socket
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server started att " + new Date() + "\n");

            //listen for a connection
            //using a regular *client* socket
            Socket socket = serverSocket.accept();

            //now, prepare to send and receive data
            //on output streams
            DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
            DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());

            //server loop listening for the client
            //and responding
            while(true) {
                String strReceived = inputFromClient.readUTF();

                String [] inputArray = strReceived.split(" ");

                if(inputArray[0].equalsIgnoreCase("LOGIN")) {
                    if(isLoggedIn){
                        outputToClient.writeUTF("You must first logout.");
                    }

                    //Check login info and send back corresponding message
                    if(inputArray.length == 3){
                        String userEntry = inputArray[1] + " " + inputArray[2];

                        if(logins.contains(userEntry)){
                            System.out.println("User " +  inputArray[1] + " logged in.");
                            outputToClient.writeUTF("SUCCESS");
                            isLoggedIn = true;
                            currentUser = inputArray[1];
                        }
                        else {
                            System.out.println("Invalid User.");
                            outputToClient.writeUTF("FAILURE: Please provide correct username and password. Try again.");
                        }
                    }
                    else {
                        outputToClient.writeUTF("You're not using the command correctly.");
                    }

                }

                else if(inputArray[0].equalsIgnoreCase("SOLVE")){
                    if(isLoggedIn){
                        String dimensions = calculateDimensions(inputArray);
                        outputToClient.writeUTF(dimensions);

                        //Create file if not already created
                        File usersFile = new File("files/"+currentUser+".txt");
                        usersFile.createNewFile();

                        //Print to file
                        FileWriter fileWriter = new FileWriter("files/"+currentUser+".txt", true);
                        PrintWriter printWriter = new PrintWriter(fileWriter);
                        printWriter.println(dimensions);
                        printWriter.close();

                    }
                }

                else if(inputArray[0].equalsIgnoreCase("LIST")){

                }

                else if(inputArray[0].equalsIgnoreCase("LOGOUT")){
                    System.out.println("User logged out.");
                    serverSocket.close();
                    socket.close();
                    isLoggedIn = false;
                    currentUser = "";
                    return true;
                }

                else if(strReceived.equalsIgnoreCase("SHUTDOWN")) {
                    System.out.println("Shutting down server...");
                    outputToClient.writeUTF("Shutting down server...");
                    serverSocket.close();
                    socket.close();
                    break;  //get out of loop
                }

                else {
                    System.out.println("Unknown command received: "
                            + strReceived);
                    outputToClient.writeUTF("Unknown command.  "
                            + "Please try again.");
                }
            }//end server loop
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }//end try-catch]
        return false;
    }//end createCommunicationLoop


    public static String calculateDimensions(String [] inputArray){
        // SOLVE -c 2
        // SOLVE -r 2
        // SOLVE -r 2 4

        String response = "";


        if(inputArray[1].contains("-c")){
            if(inputArray.length == 3){
                double area = Math.PI * Math.pow(Double.parseDouble(inputArray[2]), 2);
                double circumference = 2 * Math.PI * Double.parseDouble(inputArray[2]) ;
                response = "Circle's circumference is " + Math.round(circumference * 100.0) /100.0 + " and area is " + Math.round(area * 100.0) /100.0;
            }
            else{
                response= "ERROR: No radius found";
            }
        }
        else if(inputArray[1].contains("-r")){
            if(inputArray.length == 3) {
                double area = Math.pow(Double.parseDouble(inputArray[2]), 2);
                double perimeter = 4 * Double.parseDouble(inputArray[2]);
                response = "Rectangle's perimeter is " + Math.round(perimeter * 100.0) /100.0 + " and area is " + Math.round(area * 100.0) /100.0;
            }
            else if(inputArray.length == 4){
                double area = Double.parseDouble(inputArray[2]) * Double.parseDouble(inputArray[3]);
                double perimeter = 2 * Double.parseDouble(inputArray[2]) + 2 * Double.parseDouble(inputArray[3]);
                response = "Rectangle's perimeter is " + Math.round(perimeter * 100.0) /100.0 + " and area is " + Math.round(area * 100.0) /100.0;
            }
            else{
                response= "ERROR: No sides found";
            }

        }
        else{
            response= "ERROR: Incorrect use of SOLVE command.";
        }

        return response;
    }

}
