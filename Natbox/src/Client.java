import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;
/**
 * The Client class represents a client that communicates with a server over TCP/IP.
 * @author ROBERT SHONE – 25132687
 * @author KEURAN KISTEN – 23251646
 * @author TASHEEL GOVENDER – 25002112
 */
public class Client {

    /** The client's socket. */
    public static Socket socket;

    /** Input stream to read data from the server. */
    public static BufferedReader in;

    /** Output stream to write data to the server. */
    public static BufferedWriter out;

    /** The IP address of the client. */
    public static String IP;

    /** The MAC address of the client. */
    public static String MAC;

    /** Indicates whether the client is internal or external. */
    public static boolean internal;

    /**
     * The main method to start the client application.
     *
     * @param args The command-line arguments.
     * @throws IOException If an I/O error occurs.
     */
    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);
        while (true) {

            System.out.print("Enter IP: ");
            String ip = scanner.nextLine();

            //create socket and store input and output streams
            if (ip.matches("\\d+\\.\\d+\\.\\d+\\.\\d+") || ip.equals("localhost")) {
                try {
                    socket = new Socket(ip, 5000);
                    break; // Break out of the loop if connection successful
                } catch (IOException e) {
                    System.out.println("Server is not available. Please try again.");
                }
            } else {
                System.out.println("Invalid IP address format. Please enter a valid IP address.");
            }

        }

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        String status;
        while (true) {
            // Send whether client is internal or external
            System.out.print("Is client Internal (y/n): ");
            status = scanner.nextLine();

            if (status.equals("y")) {
                internal = true;
                break;
            } else if (status.equals("n")) {
                internal = false;
                break;
            }
        }


        out.write(status);
        out.newLine();
        out.flush();

        IP = in.readLine();
        if (IP == null) {
            System.out.println("No available IP's");
            System.exit(0);
        }

        System.out.println("\nAssigned IP: " + IP);
        MAC = hashIPAddress(IP);
        System.out.println("Mac address: " + MAC);

        // Create and start a new thread using a lambda expression
        new Thread(() -> {
            try {
                receivingPackets();
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        writingPackets(scanner);


    }
    /**
     * Method to handle user input and send packets.
     *
     * @param scanner The scanner object to read user input.
     * @throws IOException If an I/O error occurs.
     */
    private static void writingPackets(Scanner scanner) throws IOException {

        while(true) {
            if(scanner.nextLine().equals("/send")) {
                if (internal == true) {
                    System.out.print("Enter the Address: ");
                    String address = scanner.nextLine();

                    System.out.print("Enter the message: ");
                    String message = scanner.nextLine();

                    out.write(address);
                    out.newLine();
                    out.flush();

                    sendPackets(message, IP, address, 0);


                } else {
                    System.out.print("Enter the Natbox IP: ");
                    String address = scanner.nextLine();

                    if (address.equals("192.168.2.0")) {

                        System.out.print("Enter the port: ");
                        String port = scanner.nextLine();

                        if (port.equals("0")) {
                            System.out.println("Port number cant be '0'");
                            continue;
                        }

                        System.out.print("Enter the message: ");
                        String message = scanner.nextLine();

                        out.write(port);
                        out.newLine();
                        out.flush();

                        try{
                            Integer.parseInt(port);
                        } catch (NumberFormatException e) {
                            System.out.println("Packets were dropped");
                            continue;
                        }

                        sendPackets(message, IP, address, Integer.parseInt(port));

                    } else {
                        System.out.println("Destination is not Natbox address");
                    }



                }
            } else {
                System.out.println("Invalid command: Use '/send' to send message");
            }


        }

    }
    /**
     * Method to send packets.
     *
     * @param message     The message to be sent.
     * @param sender      The sender's IP address.
     * @param destination The destination IP address.
     * @param port        The destination port.
     * @throws IOException If an I/O error occurs.
     */
    public static void sendPackets(String message, String sender, String destination, int port) throws IOException {

        UDP_Packet paquet = new UDP_Packet(message, sender, destination, port, MAC);

        // Serialize the packet
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(paquet);
        objectOutputStream.close();

        // Get the serialized data
        byte[] sendData = outputStream.toByteArray();

        out.write(Arrays.toString(sendData));
        out.newLine();
        out.flush();

        System.out.println("\n------------------------");
        System.out.println("Packet Sent");
        System.out.println("------------------------");
        System.out.println("Paquet Type:  ECHO_REQUEST");
        System.out.println("Source IP:    " + IP);
        System.out.println("Source MAC:   " + MAC);
        System.out.println("Dest IP:      " + paquet.getDestination());
        System.out.println("Dest MAC:     " + hashIPAddress(paquet.getDestination()));
        System.out.println("Message:      " + paquet.getMessage());
        System.out.println("------------------------\n");

    }


    /**
     * Method to compute the SHA-256 hash of an IP address.
     *
     * @param ipAddress The IP address to hash.
     * @return The SHA-256 hash of the IP address.
     */
    public static String hashIPAddress(String ipAddress) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(ipAddress.getBytes());

            // Convert byte array to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Method to handle receiving packets from the server.
     *
     * @throws IOException            If an I/O error occurs.
     * @throws ClassNotFoundException If the class of a serialized object cannot be found.
     * @throws InterruptedException   If the thread is interrupted.
     */
    public static void receivingPackets() throws IOException, ClassNotFoundException, InterruptedException {
        String line;
        while ((line = in.readLine()) != null) {

            if (line.equals("-1")) {
                Thread.sleep(1000);
                System.out.println("\n------------------------");
                System.out.println("Packet Received");
                System.out.println("------------------------");
                System.out.println("Paquet Type:  ECHO_REPLY");
                System.out.println("Message: No IP was found");
                System.out.println("------------------------\n");

            } else if (line.equals("-2")) {
                Thread.sleep(1000);
                System.out.println("\n------------------------");
                System.out.println("Packet Received");
                System.out.println("------------------------");
                System.out.println("Paquet Type:  ECHO_REPLY");
                System.out.println("Message: Packets were received successfully");
                System.out.println("------------------------\n");

            } else {
                String[] temp = line.substring(1, line.length()-1).split(", ");
                // Convert to bytes
                byte[] receiveData = new byte[temp.length];
                for (int i = 0; i < temp.length; i++) {
                    receiveData[i] = Byte.parseByte(temp[i]);
                }

                // Deserialize the packet
                ByteArrayInputStream byteStream = new ByteArrayInputStream(receiveData);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteStream);
                UDP_Packet paquet = (UDP_Packet) objectInputStream.readObject();

                System.out.println("\n------------------------");
                System.out.println("Packet Received");
                System.out.println("------------------------");
                System.out.println("Paquet Type:  ECHO_REQUEST");
                System.out.println("Source IP:    " + paquet.getSender());
                System.out.println("Source MAC:   " + paquet.getSender_MAC());
                System.out.println("Dest IP:      " + IP);
                System.out.println("Dest MAC:     " + MAC);
                System.out.println("Message:      " + paquet.getMessage());
                System.out.println("------------------------\n");
            }

        }

        if (line == null) {
            socket.close();
            System.out.println("\nServer disconnected");
            System.exit(0);
        }
    }



}
/**
 * The UDP_Packet class represents a UDP packet.
 */
class UDP_Packet implements Serializable{

    /** The message contained in the packet. */
    private String message;

    /** The sender's IP address. */
    private String sender;

    /** The sender's MAC address. */
    private String sender_MAC;

    /** The destination IP address. */
    private String destination;

    /** The destination port. */
    private int port;

    /**
     * Constructs a UDP packet with the specified parameters.
     *
     * @param data        The message contained in the packet.
     * @param sender      The sender's IP address.
     * @param destination The destination IP address.
     * @param port        The destination port.
     * @param mac         The sender's MAC address.
     */
    public UDP_Packet(String data, String sender, String destination, int port, String mac) {
        this.message = data;
        this.sender = sender;
        this.destination = destination;
        this.port = port;
        this.sender_MAC = mac;
    }
    /**
     * Gets the message contained in the packet.
     *
     * @return The message.
     */
    public String getMessage() {
        return message;
    }
    /**
     * Gets the sender's MAC address.
     *
     * @return The sender's MAC address.
     */
    public String getSender_MAC() {
        return sender_MAC;
    }
    /**
     * Gets the sender's IP address.
     *
     * @return The sender's IP address.
     */
    public String getSender() {
        return sender;
    }
    /**
     * Sets the sender's IP address.
     *
     * @param sent The sender's IP address.
     */
    public void setSender(String sent) {
        sender = sent;
    }
    /**
     * Gets the destination IP address.
     *
     * @return The destination IP address.
     */
    public String getDestination() {
        return destination;
    }
    /**
     * Gets the destination port.
     *
     * @return The destination port.
     */
    public int getPort() {
        return port;
    }

}
