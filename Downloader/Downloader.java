package Downloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import Client.Client;
import Diary.DiaryRemote;

public class Downloader implements Runnable {
    Socket clientSocket;

    // Constructeur qui accepte le Socket
    public Downloader(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

	public static void main(String[] args) throws IOException {
		try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Downloader Server is running on port 8080...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Crée un nouveau thread pour chaque client
                new Thread(new Downloader(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public void run() {
        try (
            InputStreamReader in = new InputStreamReader(clientSocket.getInputStream());
            PrintStream out = new PrintStream(clientSocket.getOutputStream());
        ) {
            // Lecture de la requête envoyée par le client
            BufferedReader reader = new BufferedReader(in);
            String request = reader.readLine();
            System.out.println("Request received: " + request);

			// Exemple de demande : GET example.txt
            if (request.startsWith("GET ")) {
                String file_name = request.substring(4).trim();

                // Connecter au service RMI pour récupérer des informations sur le fichier
                String reconstructedContent = getReconstructedFileContent(file_name);
                // Send the reconstructed content back to the original requester
                out.println(reconstructedContent);
            } else {
                // Réponse 404 si le fichier n'existe pas
                System.out.print("Erreur ! La commande est inconnue");
            }
            clientSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

    private List<Integer> partition(int file_size,int nb_clients){
        int baseSize = file_size / nb_clients; // Taille minimale de chaque partie
        int remainder = file_size % nb_clients; // Reste à distribuer (pour équilibrer)
        
        List<Integer> repartition = new ArrayList<Integer>();
        // Répartition des morceaux
        for (int i = 1; i <= nb_clients; i++) {
            int partSize = baseSize;
            if (remainder > 0) { // Ajouter une unité au premier reste clients
                partSize++;
                remainder--;
            }
            repartition.add(partSize);
        }    
        return repartition;
    } 

    public String getReconstructedFileContent(String file_name) throws RemoteException {
    try {
        // Connecter au service RMI pour récupérer des informations sur le fichier
        DiaryRemote diary = (DiaryRemote) Naming.lookup("localhost/DiaryService");
        List<Client> clients_related = diary.getClient(file_name);

        int nb_clients = clients_related.size();

        int file_size = diary.getSize(file_name);

        if (file_size == -1){
            throw new Exception("Fichier non trouvé");
        } else if (file_size == -2) {
            throw new Exception("Aucun client trouvé possedant ce fichier");
        }

        // Partitionner le fichier entre les clients
        List<Integer> partitions = partition(file_size, nb_clients);
        // Récupérer les parties du fichier depuis chaque daemon
        Map<Client, String> fileParts = new HashMap<>();
        int startIndex = 0;
        List<Slave> slaves = new ArrayList<Slave>();
        for (int i = 0; i < clients_related.size(); i++) {
            Slave slave = new Slave(clients_related.get(i), partitions.get(i), startIndex, file_name,fileParts, this);
            slave.start();

            slaves.add(slave);
            startIndex += partitions.get(i);
        }
        for (int i = 0; i < clients_related.size(); i++) {
            slaves.get(i).join();
        }

        // Reconstruire le contenu du fichier à partir des parties
        return fileParts.values().stream().collect(Collectors.joining());

    } catch (Exception e) {
        System.out.println("Erreur lors du téléchargement du fichier : " + e.getMessage());
        return null;    }
    }


	public String fetchFilePartFromDaemon(Client client, String fileName, int start, int end) throws IOException {
        System.out.println("Attempting to connect to daemon on port: " + client.getDeamon().getPort());
        try (Socket daemonSocket = new Socket("localhost", client.getDeamon().getPort())) {
            InputStream daemonIn = daemonSocket.getInputStream();
            OutputStream daemonOut = daemonSocket.getOutputStream();

            // Send the request to the daemon
            String request = fileName + "," + start + "," + end;
            daemonOut.write(request.getBytes());
            daemonOut.flush();

            // Read the response from the daemon 
            BufferedReader reader = new BufferedReader(new InputStreamReader(daemonIn));
            StringBuilder filePart = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                filePart.append(line).append("\n");
            }
            return filePart.toString();

        } catch (IOException e) {
            System.err.println("Error communicating with daemon: " + e.getMessage());
            return "";
        }
    }
}

class Slave extends Thread {
    Client client;
    int partitionSize;
    int startIndex;
    String file_name;
    Map<Client, String> fileParts;
    Downloader downloader;

    public Slave (Client client, int partitionSize, int startIndex, String file_name, Map<Client, String> fileParts, Downloader downloader) {
        this.client =client;
        this.partitionSize = partitionSize;
        this.startIndex = startIndex;
        this.file_name = file_name;
        this.fileParts = fileParts;
        this.downloader = downloader;
    }

    public void run() {
        try {
            String filePart = downloader.fetchFilePartFromDaemon(this.client, file_name, startIndex, startIndex + this.partitionSize);
            this.fileParts.put(client, filePart);
        } catch (Exception e) {
            System.out.println("Erreur lors de la recuperation du fichier du client " + client);
        }
    }

}