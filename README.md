# CIS427_P1

## List of commands
- LOGIN
    - Allows the user to login
    - Usage: "LOGIN john john22"
- SOLVE
    - Allows the user to solve an equation
    - Usage: "SOLVE -c 6"
    - Usage: "SOLVE -r 2 4"
- LIST
    - Allows the user to list their solutions
    - Allows the root user to list all users solutions
    - Usage: "LIST"
    - Usage: "LIST -all"
- SHUTDOWN
    - Allows the user to shutdown the server
    - Usage: "SHUTDOWN"
- LOGOUT
    - Allows the user to logout and terminate both the client side application and the server connection.

## Building and running the Program
- Load the project into an IDE such as Intellij IDEA CE.
  - Alternatively compile the java source code into bytecode and run it using the terminal.
- Run the server application and allow it to initialize.
- Run the client application and allow it to initialize.
- login to the server and begin using your commands.

## Known problems and bugs
- None.

## Sample output
  ### Example 1
- C: login john john22 
- S: SUCCESS 
- C: solve -c 4 
- S: Circle's circumference is 25.13 and area is 50.27 
- C: solve -r 4 
- S: Rectangle's perimeter is 16.0 and area is 16.0 
- C: solve -r 5 6 
- S: Rectangle's perimeter is 22.0 and area is 30.0 
- C: list 
- S: john
  - radius 4: Circle's circumference is 25.13 and area is 50.27
  - sides 4: Rectangle's perimeter is 16.0 and area is 16.0
  - sides 5 6: Rectangle's perimeter is 22.0 and area is 30.0 
- C: logout
- S: 200 OK

  ###Example 2
- C: login root root22
- S: SUCCESS
- C: list -all 
- S: root
  - No interactions yet
- john
  - radius 4: Circle's circumference is 25.13 and area is 50.27
  - sides 4: Rectangle's perimeter is 16.0 and area is 16.0
  - sides 5 6: Rectangle's perimeter is 22.0 and area is 30.0
- sally
  - No interactions yet
- qiang
  - No interactions yet
- C: shutdown
- S: 200 OK