import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;

/**
 * Represents a NAT (Network Address Translation) box.
 */
public class Natbox {

    /** The IP address of the NAT box. */
    public static String natBox_IP = "192.168.2.0";
    /** List of clients connected to the NAT box. */
    public static ClientList clients;

    /**
     * Main method to start the NAT box server.
     *
     * @param args Command-line arguments
     * @throws IOException if an I/O error occurs when opening the socket
     */
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5000);
        DHCP dhcp = new DHCP();
        clients = new ClientList();
        int count = 1;

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(new ClientHandler(clientSocket, dhcp, count)).start();
            count ++;
        }

    }
    /**
     * Handles communication with a client.
     */
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedWriter writer;
        private BufferedReader reader;
        private String IP;
        private boolean internal;
        private DHCP dhcp;
        private int count;
        private int port;

        /**
         * Constructor for the client handler.
         *
         * @param socket Client socket
         * @param dhcp DHCP object for IP management
         * @param count Count of clients
         */
        public ClientHandler(Socket socket, DHCP dhcp, int count) {
            this.clientSocket = socket;
            this.dhcp = dhcp;
            this.count = count;
            this.port = count;
        }
        /**
         * Runs the client handler thread.
         */
        @Override
        public void run() {
            try {
                writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String status = reader.readLine();

                if (status.equals("y")) {
                    internal = true;
                    IP temp = dhcp.get_Available_IP();

                    if (temp == null) {
                        System.out.println("No available IP's\n");

                    } else {
                        IP = temp.get_IP();

                        System.out.println("New Internal user joined with IP: " + IP);

                        writer.write(IP);
                        writer.newLine();
                        writer.flush();
                    }


                } else if (status.equals("n")) {
                    internal = false;
                    IP = get_External_IP(count);
                    System.out.println("New External user joined with IP: " + IP);
                    port = 0;
                    writer.write(IP);
                    writer.newLine();
                    writer.flush();
                }

                if (IP != null) {
                    // Add new client to hash-map
                    ClientData client = new ClientData(reader, writer, internal, port);
                    clients.addClient(IP, client);

                    displayNatTable();

                    // Handle client messages
                    String address;
                    String message;
                    int port;

                    while (true) {

                        // If client is internal
                        if (internal == true) {

                            address = reader.readLine();

                            if(address == null) {
                                dhcp.clientDisconnected(IP);
                                clients.removeClientData(IP);
                                System.out.println("This address: " + IP  + " has disconnected");
                                displayNatTable();
                                break;
                            }

                            ClientData destination = clients.getClientData(address);

                            if (destination != null) {

                                // If destination is internal
                                if (destination.getInternal() == true) {

                                    // Receive packet
                                    String packetBytes = reader.readLine();

                                    // Send packet to destination address
                                    BufferedWriter bf = destination.getWriter();
                                    bf.write(packetBytes);
                                    bf.newLine();
                                    bf.flush();

                                    writer.write("-2");
                                    writer.newLine();
                                    writer.flush();

                                    // If destination is external
                                } else {

                                    // Receive packet
                                    String packetBytes = reader.readLine();

                                    UDP_Packet paquet = getPacketData(packetBytes);

                                    paquet.setSender(natBox_IP);

                                    // Serialize the packet
                                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                                    objectOutputStream.writeObject(paquet);
                                    objectOutputStream.close();

                                    // Get the serialized data
                                    byte[] sendData = outputStream.toByteArray();

                                    // Send packet to destination address
                                    BufferedWriter bf = destination.getWriter();
                                    bf.write(Arrays.toString(sendData));
                                    bf.newLine();
                                    bf.flush();

                                    writer.write("-2");
                                    writer.newLine();
                                    writer.flush();

                                }
                            } else {
                                String packetBytes = reader.readLine();
                                System.out.println("Packets were dropped");

                                writer.write("-1");
                                writer.newLine();
                                writer.flush();
                            }

                            // If client is external
                        } else {

                            String cur = reader.readLine();


                            if(cur == null) {
                                dhcp.clientDisconnected(IP);
                                clients.removeClientData(IP);
                                System.out.println("This address: " + IP  + " has disconnected");
                                displayNatTable();
                                break;
                            }

                            try{
                                Integer.parseInt(cur);
                            } catch (NumberFormatException e) {
                                System.out.println("Packets were dropped");
                                continue;
                            }

                            port = Integer.parseInt(cur);


                            // Get client who has the same port
                            ClientData destination = clients.getClientFromPort(port);

                            if (destination != null) {

                                // If destination is internal
                                if (destination.getInternal() == true) {

                                    // Receive packet
                                    String packetBytes = reader.readLine();

                                    // Send packet to destination
                                    BufferedWriter bf = destination.getWriter();
                                    bf.write(packetBytes);
                                    bf.newLine();
                                    bf.flush();

                                    writer.write("-2");
                                    writer.newLine();
                                    writer.flush();

                                    // If destination is external
                                } else {
                                    System.out.println("Packets were dropped as external->external");
                                }
                            } else {
                                String packetBytes = reader.readLine();
                                System.out.println("Packets were dropped");
                                writer.write("-1");
                                writer.newLine();
                                writer.flush();
                            }

                        }


                    }
                }

                clientSocket.close();



            } catch (IOException e) {

            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        /**
         * Deserialize packet data from the client.
         *
         * @param line Serialized packet data
         * @return Deserialized UDP packet
         * @throws IOException            if an I/O error occurs
         * @throws ClassNotFoundException if the class of the serialized object cannot be found
         */
        public UDP_Packet getPacketData(String line) throws IOException, ClassNotFoundException {

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

            return paquet;

        }
        /**
         * Generates an external IP address based on the given count.
         *
         * @param count Count of clients
         * @return External IP address
         */
        public String get_External_IP(int count) {
            Random random = new Random();

            // Generates a random number between 100 and 255
            int num1 = random.nextInt(156) + 100;
            int num2 = random.nextInt(156) + 100;

            String out = num1 + "." + num2 + ".1." + count;
            return out;
        }
    }
    /**
     * Displays the NAT table showing internal IP addresses mapped to ports.
     */
    public static void displayNatTable() {
        System.out.println("\n-------------------------");
        System.out.println("Nat Table: (IP   ->  port)");
        System.out.println("-------------------------");

        for (String temp_IP : clients.clientList().keySet()) {
            // Only IPs that are internal
            ClientData temp_CD = clients.getClientData(temp_IP);
            if (temp_CD.getInternal() == true) {
                System.out.println(temp_IP + "  ->  " + temp_CD.getPort());
            }
        }

        System.out.println("-------------------------\n");
    }

}
