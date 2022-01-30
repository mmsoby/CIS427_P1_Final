
package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;//Added cause I want to
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
            //result stores the value of the createCommunicationLoop() so that after
            //someone logs out the server still runs.
            result = createCommunicationLoop();
        } while(result);
    }//end main

    //This function loads all the registered users into an arraylist from the file
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
            System.out.println("Server started at " + new Date() + "\n");

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
                System.out.println(strReceived);

                String [] inputArray = strReceived.split(" ");

                if(inputArray[0].equalsIgnoreCase("LOGIN")) {
                    if(isLoggedIn){
                        outputToClient.writeUTF("You must first logout.");
                        continue;
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
                        outputToClient.writeUTF("301 message format error");
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
                        printWriter.println(Arrays.asList(inputArray).toString()); // Mohsen's change
                        printWriter.println(dimensions);
                        printWriter.close();

                    }
                }

                else if(inputArray[0].equalsIgnoreCase("LIST")){
                    if(isLoggedIn) {
                        String listInformation = "";
                        File usersFile = new File("files/" + currentUser + ".txt");

                        //Case 1: -all flags
                        if (inputArray.length == 2 && inputArray[1].equals("-all")) {
                            if(currentUser.equals("root")){
                                String allInfo = getListInformation("root",  new File("files/root.txt"))   +
                                                 getListInformation("john",  new File("files/john.txt"))   +
                                                 getListInformation("sally", new File("files/sally.txt"))  +
                                                 getListInformation("qiang", new File("files/qiang.txt"));
                                outputToClient.writeUTF(allInfo);
                            }
                            else{
                                outputToClient.writeUTF("Error: you are not the root user");
                            }
                        }
                        //Case 2: File doesn't exist
                        else if (!usersFile.exists() || usersFile.length() == 0) {
                            listInformation += currentUser + "\n";
                            listInformation += "\t" + "No interactions yet";
                            outputToClient.writeUTF(listInformation);
                        }
                        //Case 3: no flags
                        else if(inputArray.length == 1) {
                            outputToClient.writeUTF(getListInformation(currentUser, usersFile));
                        }

                        //Case 4: Command not correct
                        else{
                            outputToClient.writeUTF("301 message format error");
                        }
                    }
                }

                else if(strReceived.equalsIgnoreCase("LOGOUT")){
                    System.out.println("User "+currentUser+" logged out.");
                    outputToClient.writeUTF("200 OK");
                    serverSocket.close();
                    socket.close();
                    isLoggedIn = false;
                    currentUser = "";
                    return true;
                }

                else if(strReceived.equalsIgnoreCase("SHUTDOWN")) {
                    System.out.println("Shutting down server...");
                    outputToClient.writeUTF("200 OK");
                    serverSocket.close();
                    socket.close();
                    break;  //get out of loop
                }

                else {
                    outputToClient.writeUTF("300 invalid command");
                }
            }//end server loop
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }//end try-catch]
        return false;
    }//end createCommunicationLoop

    //This function gets the list information from the files and prints them out
    public static String getListInformation(String name, File usersFile){
        String listInformation = "";
        try {
            listInformation = name + "\n";
            //If file exists and is not empty
            if(usersFile.exists() && usersFile.length() > 0) {
                Scanner s = new Scanner(usersFile);
                while (s.hasNextLine()) {
                    String data = s.nextLine().substring(1).replaceFirst("]", "").replace(",", ""); //Get next Array of data
                    String output = s.nextLine(); //Get next solve information output


                    if (output.charAt(0) == 'E') { //If output is an Error Type
                        listInformation += "\t\t" + output + "\n";
                    } else if (output.charAt(0) == 'R') { //If output is a Rectangle/Square
                        listInformation += "\t\t" + "sides" + data.substring(8) + ": " + output + "\n";
                    } else if (output.charAt(0) == 'C') { //If output is a Circle
                        listInformation += "\t\t" + "radius" + data.substring(8) + ": " + output + "\n";
                    }
                }
            }
            // File doesn't exist or is empty
            else {
                listInformation += "\t\t" + "No interactions yet\n";
            }
        }
        catch(FileNotFoundException e){
            System.out.println("File for List Information not Found");
        }
        return listInformation;
    }

    //Calculates the dimensions of the string array given and returns a string response of the answer
    public static String calculateDimensions(String [] inputArray){
        String response = "";

        if(inputArray[1].contains("-c")){
            if(inputArray.length == 3){
                double area = Math.PI * Math.pow(Double.parseDouble(inputArray[2]), 2);
                double circumference = 2 * Math.PI * Double.parseDouble(inputArray[2]) ;
                response = "Circle's circumference is " + Math.round(circumference * 100.0) /100.0 + " and area is " + Math.round(area * 100.0) /100.0;
            }
            else if(inputArray.length == 2){
                response= "ERROR: No radius found";
            }
            else{
                response ="301 message format error";
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
            else if(inputArray.length == 2){
                response= "ERROR: No sides found";
            }
            else{
                response= "301 message format error";
            }

        }
        else{
            response= "301 message format error";
        }

        return response;
    }

}
