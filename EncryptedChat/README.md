# SecureChat
The project contains three folders for part 1, 2 and 3 of the assignment statement.

PlainText/ implements the unsecure chat application
Encrypted/ further implements secure chat application using RSA encryption
Attested/ further implements encrypted messages sent with signatures.

Each folder contains two subfolders named Server/ and Client/
Server/ contains source code for running server and Client/ for running client.

# How to Run
The method to run the program for all three is same.

1. Compile the client program using javac Client.java CryptFuncs.java on the client machine
2. Compile the server program using javac Server.java CryptFuncs.java on the server machine
3. For server, run using ``server port_1 port2`` (default value of port 1 = 5001 and 5002 for port 2)
4. For client, run using ``client "username" server_ip server_port1 server_port2``

# Interface

- Client automatically registers with the server for sending and receiving messages
- In case the username is already registered, it prompts for another username
- To send a message to a user type ``@username: <some message>``
- On successful sent, the client will receive an acknowledgement with ``SENT username``

# Notes

- The code contained in folder Attested/ implements everything that is implemented in Encrypted/ and Plaintext/
- In case of any error in the other folder, Attested/ may be assumed as the final working code.