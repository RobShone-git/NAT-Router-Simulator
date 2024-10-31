
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;

/**
 * Stores sll the online clients so that it is easier to access and manage
 *
 * @author ROBERT SHONE – 25132687
 * @author KEURAN KISTEN – 23251646
 * @author TASHEEL GOVENDER – 25002112
 */
public class ClientList {
    private HashMap<String, ClientData> clients;
    /**
     * Constructs a new ClientList object.
     */
    public ClientList() {
        clients = new HashMap<>();
    }
    /**
     * Returns the HashMap containing the list of clients.
     *
     * @return The HashMap containing client data.
     */
    public HashMap<String, ClientData> clientList() {
        return clients;
    }
    /**
     * Retrieves a client from the list based on the port number.
     *
     * @param port The port number of the client.
     * @return The ClientData object associated with the given port, or null if not found.
     */
    public ClientData getClientFromPort(int port) {
        for (ClientData cd : clients.values()) {
            if(cd.getPort() == port) {
                return cd;
            }
        }
        return null;
    }

    /**
     * Add client to the hashmap
     *
     * @param IP ip address of client
     * @param client client object of client
     */
    public void addClient(String IP, ClientData client) {
        clients.put(IP, client);
    }

    /**
     *  Get a client by their ip
     *
     * @param IP ip address of client
     * @return Client object of matching ip
     */
    public ClientData getClientData(String IP) {
        return clients.get(IP);
    }


    /**
     * Remove a client by username
     *
     * @param IP User to be removed
     */
    public void removeClientData(String IP) {
        clients.remove(IP);
    }


}
/**
 * Contains information about each client
 *
 * @author ROBERT SHONE – 25132687
 * @author KEURAN KISTAN – 23251646
 * @author TASHEEL GOVENDER – 25002112
 */
class ClientData {
    private BufferedReader reader;
    private BufferedWriter writer;
    private boolean internal;
    private int port;

    /**
     * Constructs a new ClientData object with the specified parameters.
     *
     * @param reader   The BufferedReader associated with the client.
     * @param writer   The BufferedWriter associated with the client.
     * @param internal Indicates whether the client is internal.
     * @param port     The port number of the client.
     */
    public ClientData(BufferedReader reader, BufferedWriter writer, boolean internal, int port) {
        this.reader = reader;
        this.writer = writer;
        this.internal = internal;
        this.port = port;
    }

    /**
     * Returns clients reader
     *
     * @return clients reader
     */
    public BufferedReader getReader() {
        return reader;
    }

    public int getPort() {
        return port;
    }

    /**
     * Returns clients writer
     *
     * @return clients writer
     */
    public BufferedWriter getWriter() {
        return writer;
    }

    /**
     * Returns clients internal status
     *
     * @return clients internal status
     */
    public boolean getInternal() {
        return internal;
    }


}

