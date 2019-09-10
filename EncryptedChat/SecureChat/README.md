# SecureChat
The project contains two folders, one for Server and one for Client
- Server/ contains source code for running server and Client/ for running client.
- Both server and client take the last input as `mode` which takes values `1`,` 2` or `3`, which runs the chat application in plaintext, encrypted and encrypted+ signatured messages.

# How to Run

1. Compile the client program using `javac Client.java` on the client machine
2. Compile the server program using `javac Server.java` on the server machine
3. For server, run using ``server port_1 port2 mode`` 
4. For client, run using ``client "username" server_ip server_port1 server_port2 mode``

# Interface

- Client automatically registers with the server for sending and receiving messages
- In case the username is already registered, it prompts for another username
- To send a message to a user type ``@username: <some message>``
- On successful sent, the client will receive an acknowledgement with ``SENT username``


