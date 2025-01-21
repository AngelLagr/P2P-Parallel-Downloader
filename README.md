# PeerToPeerProject

## Description

This project is a proof of concept (PoC) developed in Java to demonstrate parallelization, data compression, and disconnection handling in a peer-to-peer downloader. The goal is to enable file sharing between machines without a central server while simulating realistic network conditions, such as slow connections.

## Features

- **Parallel Downloading**: Files are divided into segments and downloaded simultaneously to improve performance.
- **Data Compression**: Reduces the size of transferred files to optimize exchanges.
- **Disconnection Handling**: If a peer disconnects during a download, the system redistributes the missing segments to other active peers.

## Project Structure

- `Client/`: Source code for client machines performing downloads.
- `Downloader/`: Components responsible for managing downloads and segments.
- `Diary/`: Central server acting as a coordinator and peer manager.
- `downloads/`: Directory to store downloaded files.
- `Rapport_Intergiciel.pdf`: A french report detailing the project's functionality and objectives.

## Installation

1. **Clone the repository**:

   ```bash
   git clone https://github.com/AngelLagr/PeerToPeerProject.git
   ```

2. **Compile the source files**:

   ```bash
   javac */*.java
   ```

## Usage

### Starting the Server (Diary)

The Diary server coordinates interactions between clients. Start it on a machine with the following command:

```bash
java Diary.DiaryServer
```

### Starting the Clients

On client machines, run the following command to start the peer-to-peer client:

```bash
java Client.Client <identifier> <Diary_IP> <delay>
```

- **`identifier`**: A unique identifier for each client.
- **`Diary_IP`**: The IP address or hostname of the Diary server.
- **`delay`**: Time in milliseconds to simulate a slow connection. This delay is added to every packet sent.

### Execution Example

1. Start the Diary server:

   ```bash
   java Diary.DiaryServer
   ```

2. Start two clients on different machines:

   ```bash
   java Client.Client client1 192.168.1.10 100
   java Client.Client client2 192.168.1.10 200
   ```

In this example:
- `client1` communicates with a delay of 100ms per packet.
- `client2` communicates with a delay of 200ms per packet.

## Key Technical Points

1. **Parallelization**: Each file is segmented into independent parts. Segments are downloaded in parallel from different peers, optimizing performance.
2. **Data Compression**: Segments are compressed before transmission to minimize bandwidth usage.
3. **Disconnection Handling**: If a peer disconnects before completing its share, other clients automatically resume the missing downloads.

## Note

This PoC only works within the same network and does not support downloads across machines outside the network.

## License

This project is licensed under the MIT License. See the `LICENSE` file for more details.
