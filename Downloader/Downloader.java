package Downloader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import Client.Client;
import Diary.ClientRepresentation;
import Diary.DiaryRemote;

public class Downloader {
    Client client;

    // Constructeur qui accepte le Socket
    public Downloader(Client client) {
        this.client = client;
    }

    //Si y'a qu'un seul client c'est bizarre
    private List<Long> partition(Long file_size,int nb_clients){
        Long baseSize = file_size / nb_clients; // Taille minimale de chaque partie
        Long remainder = file_size % nb_clients; // Reste à distribuer (pour équilibrer)
        
        List<Long> repartition = new ArrayList<Long>();
        // Répartition des morceaux
        for (int i = 1; i <= nb_clients; i++) {
            Long partSize = baseSize;
            if (remainder > 0) { // Ajouter une unité au premier reste clients
                partSize++;
                remainder--;
            }
            repartition.add(partSize);
        }    
        return repartition;
    } 

    public byte[] download(String file_name) throws RemoteException {
        try {
            // Connecter au service RMI pour récupérer des informations sur le fichier
            Registry reg = LocateRegistry.getRegistry(this.client.getDiaryIp(),1099);
            DiaryRemote diary = (DiaryRemote) reg.lookup("DiaryService");        

            List<ClientRepresentation> clients_related = diary.getClient(file_name);
            int nb_clients = clients_related.size();
            if (nb_clients == 0) {
                throw new Exception("Aucun client ne possède ce fichier");
            }

            Long file_size = diary.getFileSizeDiary(file_name); 

            if (file_size == -1){
                throw new Exception("Fichier non trouvé");
            } else if (file_size == -2) {
                throw new Exception("Aucun client trouvé possedant ce fichier");
            }

            // Partitionner le fichier entre les clients
            List<Long> partitions = partition(file_size, nb_clients);
            // Récupérer les parties du fichier depuis chaque daemon
            List<byte[]> fileParts = new ArrayList<>();
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
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            for (byte[] part : fileParts) {
                byteArrayOutputStream.write(part);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            System.out.println("Erreur lors du téléchargement du fichier : " + e.getMessage());
            return null;    
        }
    }


	public byte[] fetchFilePartFromDaemon(ClientRepresentation client, String fileName, Long start, Long end) throws IOException {
        System.out.println("Attempting to connect to daemon on port: " + client);
        try (Socket daemonSocket = new Socket( client.getAdresse(), client.getPort())) {
            InputStream daemonIn = daemonSocket.getInputStream();
            OutputStream daemonOut = daemonSocket.getOutputStream();

            // Send the request to the daemon
            String request = "GetFilePart " + fileName + "," + start + "," + end;
            daemonOut.write(request.getBytes());
            daemonOut.flush();
            
            

            // Lire le fichier en morceaux
            byte[] fragment = new byte[1024];
            int totalBytesRead = 0;
            int bytesRead;
            byte[] filePart = new byte[(int)(end-start)]; 

            int index = 0;
            while ((bytesRead = daemonIn.read(fragment)) != -1) {
                totalBytesRead += bytesRead;
                System.arraycopy(fragment,0,filePart,index,bytesRead);
                if (totalBytesRead >= (end - start)) {
                    break;
                }
                index+=bytesRead;
                fragment = new byte[1024];
            }
            
            // Si on n'a pas lu suffisamment de données, renvoyer une erreur
            if (totalBytesRead < (end - start)) {
                throw new IOException("Received less data than expected");
            }
                
            return filePart;

        } catch (IOException e) {
            System.err.println("Error communicating with daemon: " + e.getMessage());
            return null;
        }
    }
}

class Slave extends Thread {
    ClientRepresentation client;
    Long partitionSize;
    int startIndex;
    String file_name;
    List<byte[]> fileParts;
    Downloader downloader;
    Integer index_client;

    public Slave (ClientRepresentation client, Long partitionSize, int startIndex, String file_name, List<byte[]> fileParts, Downloader downloader, Integer index_client) {
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
            byte[] filePart = downloader.fetchFilePartFromDaemon(this.client, file_name, (long) (startIndex), startIndex + this.partitionSize);
            this.fileParts.set(index_client, filePart);
        } catch (Exception e) {
            System.out.println("Erreur lors de la recuperation du fichier du client " + client);
        }
    }

}