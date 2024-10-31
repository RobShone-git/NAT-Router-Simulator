# NAT Router Simulator with DHCP and Paquet Forwarding

This project simulates a basic Network Address Translation (NAT) router with a minimal Dynamic Host Configuration Protocol (DHCP) and paquet forwarding functionality, allowing for data transfer between simulated clients on a local or external network. The implementation includes support for basic NAT functionalities, paquet modification, and connection management to provide insights into how NAT influences network behavior and communication.

## Project Overview

### Key Components

1. **NAT Router**:
   - Acts as the main server that handles paquet routing between internal and external clients.
   - Modifies paquet headers for outbound traffic and maintains a NAT translation table for address and port binding.
   - TCP paquet forwarding.
   - Includes a dynamically refreshing NAT table for tracking active connections.

2. **Clients**:
   - Can connect as internal or external clients.
   - Internal clients request IP addresses through a minimal DHCP server implemented on the NAT-box.
   - Supports TCP with simple paquet responses.

### Project Specifications

#### NAT-Box Implementation

- **IP Address Assignment**: Assigns unique IP addresses to internal clients via DHCP.
- **Address Translation Table**: Maintains and dynamically refreshes a NAT table for tracking internal-to-external connections.
- **NAT Handling Rules**:
  - **Internal -> Internal**: Paquets are forwarded without modification.
  - **Internal -> External**: Paquet headers are modified and logged in the NAT table.
  - **External -> Internal**: Paquets are routed if an entry exists in the NAT table; otherwise, they are dropped.
  - **External -> External**: Paquets are dropped as they are outside the NAT-box's routing scope.
- **MAC Address Assignment**: Assigns unique (simulated) MAC addresses for each client.

#### Client Implementation

- **Roles**: Clients can act as internal or external devices.
- **IP and MAC Assignment**: Internal clients use DHCP to request IP addresses and are assigned unique MAC addresses.
- **CLI-Based**: All client interactions are handled through the command-line interface.
- **Reply Handling**: Each paquet received from another client is acknowledged with an appropriate reply, such as an ACK.

### Protocol Support

The simulator minimally supports:
- **ICMP**: For echo requests and error paquet handling.
- **TCP**: For basic paquet forwarding.
- **DHCP**: For assigning IP addresses to internal clients.
