# Report on extension of chat application

1. Unregistering user if he goes offline without explicitly unregistering (using ^C):

-  Implemented by checking if read string is null and then deregistering the user and catching exception.

2. Simulating Whatsapp, `SENT`, `DELIVERED`, `READ` acks & Offline Users. 

- Each user has two associated mailbox, one for sending and other for receiving messages at the server which will be two `Piped input output stream`, for sending and receiving.
- Output end of sending pipe and input end of receiving pipe simulates connection between client and mailbox, and input end of sending and output end of receiving simulates connection between 2 mailboxes.

- A th (T1) associated with the client reads data from the socket's input stream and keeps on appending 
data to the client's sending pipe output stream. This ensures that as soon as a message arrives, it is saved on the server (Mailbox). (_Worker between client and mailbox_)

- A thread (T2) associated with the client reads data on this sending pipe and grabs the 
respective receiver's receiving pipe output stream. It waits for the pipe to be free (through use of a lock) and appends the message to the stream. (_Worker between mailbox and mailbox_)

- A thread (T3) associated with the client reads data from the receving pipe input, and as soon the user 
gets online, appends the message to the client receiving socket output stream. (_Worker between mailbox and client_)

                
    CLIENT 1 ===== MAILBOX 1 ===============  MAILBOX 2  ===== CLIENT 2 
                            =              =
                             =            =
                              = MAILBOX 3=
                                    ||
                                    ||
                                    ||
                                CLIENT 3
   
- To give acknowledgments of sent, delivered and read, to the user, the following protocol can be used:

- First, when a client sends a message, it also attaches an id with it. This is to allow the client
to infer which message is the ack ment for. The id can be reused once its acks have been received.

- The acks will be sent along the normal send receive channels, it is on the client (end user) to 
determine, whether it is a message or an ack. The threads associated with the client on the server
end also know whether its ack or notmal message. This is important to avoid acks of acks.

- `SENT` means message sent to mailbox, `DELIVERED` means message sent to the client.
- `READ` is the ack which is explicitly sent by the client when it actually reads the message. The definition
of 'read' will be determined by the application at the client end.

- When T3 sees a (non ack) message on its mailbox, it puts a SENT ack to the sending end of its mailbox, 
so that T2 can later send this ack to the sender mailbox. This action is trigerred only on non -ack 
messages to avoid recursive acks of ack. Another thread can be employed for this so that T3 can remain
employed for sending messages to the client.

- When client application sees a message, it sends a `DELIVERED` ack on its socket's send output stream, 
just like any ordinary message.

- On performing application based read operation, it sends another `READ` ack on its socket's send output
stream.

- Note that how ``DELIVERED`` and `READ` acks are on the client application control, but not the SENT ack, because it is managed by application running at the mailbox server.

- To implement this in our program, we will usually consider mailboxes as different threads/ processes on the
central server. One can also have mailbox server applications and then mailboxes establishing connections with 
each other. They however will need a central directory server to access and establish connection with other
mailboxes. However a distributed directory or atleast a hierarchichal directory system (like the DNS) is possible.



