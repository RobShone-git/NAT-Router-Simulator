import java.util.ArrayList;
import java.util.List;

/**
 * DHCP class manages the allocation and deallocation of IP addresses.
 */
public class DHCP {

    private List<IP> pool_IPs;
    private  int num_of_IP = 10;
    /**
     * Constructs a DHCP object and initializes the pool of IP addresses.
     */
    public DHCP() {
        pool_IPs = new ArrayList<>();

        // Create Internal IP addresses in pool
        // Internal is represented as the 0 in 3rd space
        for (int i = 1; i <= num_of_IP; i++) {
            pool_IPs.add(new IP("192.168.0." + i));
        }
    }
    /**
     * Retrieves an available IP address from the pool.
     *
     * @return An available IP address if found, otherwise null.
     */
    public IP get_Available_IP() {
        for (IP cur : pool_IPs) {
           if (cur.get_Available() == true) {
               cur.set_Available(false);
               return cur;
           }
        }
        return null;
    }
    /**
     * Marks the IP address associated with the disconnected client as available.
     *
     * @param ip The IP address of the disconnected client.
     */
    public void clientDisconnected(String ip) {
        for (IP cur : pool_IPs) {
            if (cur.get_IP().equals(ip)) {
                cur.set_Available(true);
            }
        }

    }

}
/**
 * Represents an IP address.
 */
class IP {

    private String ip;
    private boolean available;
    /**
     * Constructs an IP object with the specified IP address.
     *
     * @param ip The IP address.
     */
    public IP(String ip) {
        this.ip = ip;
        available = true;
    }
    /**
     * Checks if the IP address is available.
     *
     * @return True if the IP address is available, false otherwise.
     */
    public boolean get_Available() {
        return available;
    }
    /**
     * Sets the availability status of the IP address.
     *
     * @param bool The availability status.
     */
    public void set_Available(boolean bool) {
        available = bool;
    }

    /**
     * Retrieves the IP address.
     *
     * @return The IP address.
     */
    public String get_IP() {
        return ip;
    }


}
