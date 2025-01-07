package Downloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import Client.Client;
import Diary.DiaryRemote;

public class Downloader {
    private Client client;

    // Constructeur qui accepte le Socket
    public Downloader(Client client) {
        this.client = client;
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

    public String download(String file_name) throws RemoteException {
    try {
        // Connecter au service RMI pour récupérer des informations sur le fichier
        Registry reg = LocateRegistry.getRegistry("localhost",1099);
        DiaryRemote diary = (DiaryRemote) reg.lookup("DiaryService");        

        List<Integer> clients_related = diary.getClient(file_name);
        int nb_clients = clients_related.size();
        if (nb_clients == 0) {
            throw new Exception("Aucun client ne possède ce fichier");
        }
        int file_size = -2;
        Slave2 slave2 = new Slave2(clients_related.get(0), file_name, file_size, this, 0);
        slave2.start();
        slave2.join();

        file_size = slave2.getFileSize(); 

        if (file_size == -1){
            throw new Exception("Fichier non trouvé");
        } else if (file_size == -2) {
            throw new Exception("Aucun client trouvé possedant ce fichier");
        }

        // Partitionner le fichier entre les clients
        List<Integer> partitions = partition(file_size, nb_clients);
        // Récupérer les parties du fichier depuis chaque daemon
        List<String> fileParts = new ArrayList<String>();
        int startIndex = 0;
        List<Slave> slaves = new ArrayList<Slave>();

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < clients_related.size(); i++) {
            Slave slave = new Slave(clients_related.get(i), partitions.get(i), startIndex, file_name,fileParts, this, i);
            fileParts.add(null);
            slave.start();

            slaves.add(slave);
            startIndex += partitions.get(i);
        }
        for (int i = 0; i < clients_related.size(); i++) {
            slaves.get(i).join();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("\nDurée du téléchargement parrallèle : " + (endTime-startTime) +"ms");
        
        // Reconstruire le contenu du fichier à partir des parties
        return fileParts.stream().collect(Collectors.joining());

    } catch (Exception e) {
        System.out.println("Erreur lors du téléchargement du fichier : " + e.getMessage());
        return null;    }
    }


	public String fetchFilePartFromDaemon(Integer client, String fileName, int start, int end) throws IOException {
        System.out.println("Attempting to connect to daemon on port: " + client);
        try (Socket daemonSocket = new Socket("localhost", client)) {
            InputStream daemonIn = daemonSocket.getInputStream();
            OutputStream daemonOut = daemonSocket.getOutputStream();

            // Send the request to the daemon
            String request = "GetFilePart " + fileName + "," + start + "," + end;
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

    public Integer fetchFileSizeFromDaemon(Integer client, String fileName) throws IOException {
        System.out.println("Attempting to connect to daemon on port: " + client);

        try (Socket daemonSocket = new Socket("localhost", client);
            InputStream daemonIn = daemonSocket.getInputStream();
            OutputStream daemonOut = daemonSocket.getOutputStream()) {

            // Envoi de la requête au daemon
            String request = "getFileSize " + fileName;
            daemonOut.write(request.getBytes());
            daemonOut.flush();

            // Lecture de la réponse
            BufferedReader reader = new BufferedReader(new InputStreamReader(daemonIn));
            String response = reader.readLine();
            if (response == null || response.isEmpty()) {
                throw new IOException("Empty response from daemon.");
            }

            // Conversion de la réponse en entier
            try {
                return Integer.parseInt(response.trim());
            } catch (NumberFormatException e) {
                throw new IOException("Invalid response format: " + response, e);
            }

        } catch (IOException e) {
            System.err.println("Error communicating with daemon: " + e.getMessage());
            return -3;
        }
}
}

class Slave extends Thread {
    Integer client;
    int partitionSize;
    int startIndex;
    String file_name;
    List<String> fileParts;
    Downloader downloader;
    Integer index_client;

    public Slave (Integer client, int partitionSize, int startIndex, String file_name, List<String> fileParts, Downloader downloader, Integer index_client) {
        this.client =client;
        this.partitionSize = partitionSize;
        this.startIndex = startIndex;
        this.file_name = file_name;
        this.fileParts = fileParts;
        this.downloader = downloader;
        this.index_client = index_client;
    }

    public void run() {
        try {
            String filePart = downloader.fetchFilePartFromDaemon(this.client, file_name, startIndex, startIndex + this.partitionSize);
            this.fileParts.set(index_client, filePart);
        } catch (Exception e) {
            System.out.println("Erreur lors de la recuperation du fichier du client " + client);
        }
    }

}

class Slave2 extends Thread {
    Integer client;
    String file_name;
    Downloader downloader;
    Integer index_client;
    Integer file_size; 

    public Slave2 (Integer client, String file_name, Integer file_size, Downloader downloader, Integer index_client) {
        this.client =client;
        this.file_name = file_name;
        this.file_size = file_size;
        this.downloader = downloader;
        this.index_client = index_client;
    }

    public void run() {
        try {
            this.file_size = this.downloader.fetchFileSizeFromDaemon(this.client, file_name);
        } catch (Exception e) {
            System.out.println("Erreur lors de la recuperation du fichier du client " + this.client);
        }
    }

    public Integer getFileSize() {
        return this.file_size;
    }

}