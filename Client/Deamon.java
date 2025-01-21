package Client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;

public class Deamon implements Serializable {
    Client client;
    Map<String, File> files;
    int port;
    int delay;

    public Deamon(Client client) {
        this.client = client;
        this.files = new HashMap<String, File>();
        this.port = 8080 + client.getId();
        startClientServer();
        this.delay = client.delay;
    }

    public int getPort(){
        return port;
    }

    // J'obtiens une partie du fichier en fonction de comment je le partitionne
    public byte[] getFilePart(String filepath, int start, int end) throws IOException {
        byte[] result = new byte[end - start];
        try (RandomAccessFile reader = new RandomAccessFile(filepath, "r")) {
            reader.seek(start);
            reader.readFully(result);
        }
        // Compression des données lues
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(result);
        }
        
        return byteArrayOutputStream.toByteArray();  // Renvoie la version compressée des données
    }


    // Méthode pour démarrer l'écouteur du Client sur le port spécifique
    public void startClientServer() {
        new Thread(() -> {
            try {
                @SuppressWarnings("resource")
                ServerSocket ss = new ServerSocket(getPort());  // Écoute sur le port spécifique au client
                System.out.println("Deamon server started on port " + getPort() + " for client");

                // Boucle pour accepter les connexions du Client
                while (true) {
                    Socket s = ss.accept();
                    Slave slave = new Slave(s, this);
                    slave.start();
                }
            } catch (IOException e) {
                System.out.println("Error in Client server: " + e);
            }
        }).start();
    }

    // Cette méthode ajoute un file au Daemon
    public void addFile(File file) {
        this.files.put(file.getName(), file);
    }
}

class Slave extends Thread {
    Socket s1;
    Deamon deamon;
    int delay;

    public Slave (Socket s, Deamon deamon) {
        this.s1 =s;
        this.deamon = deamon;
        this.delay = deamon.delay;
    }

    public void run() {        
        try {            
            InputStream s1_in = s1.getInputStream();
            OutputStream s1_out = s1.getOutputStream();

            // Lire la requête du downloader
            byte[] requestBuffer = new byte[1024];
            int bytesRead = s1_in.read(requestBuffer);
            String request = new String(requestBuffer, 0, bytesRead);

            if (request.startsWith("GetFilePart")) {
                request = request.substring(12).trim();

                // La requête contient : "filename,start,end"
                String[] parts = request.split(",");
                String fileName = parts[0];
                int start = Integer.parseInt(parts[1]);
                int end = Integer.parseInt(parts[2]);

                // Lire la partie demandée du fichier
                String filePath = this.deamon.files.entrySet().stream()
                                .filter(file -> file.getKey().equals(fileName))
                                .findFirst()
                                .orElseThrow(() -> new IOException("File not found"))
                                .getValue().getAbsolutePath();
                byte[] compressedFilePart = this.deamon.getFilePart(filePath, start, end);

                // Pour envoyer les données par paquet, on peut les découper en morceaux plus petits
                int packetSize = 1024; // Taille des paquets, ajustez en fonction des besoins
                int totalLength = compressedFilePart.length;

                // Envoi par paquets avec délai simulé
                for (int i = 0; i < totalLength; i += packetSize) {
                    int length = Math.min(packetSize, totalLength - i);
                    byte[] packet = new byte[length];
                    System.arraycopy(compressedFilePart, i, packet, 0, length);

                    // Envoi du paquet
                    s1_out.write(packet);
                    s1_out.flush();

                    // Simuler un délai entre l'envoi de chaque paquet
                    try{Thread.sleep(this.delay);}catch (Exception e) {} // Pause pour simuler la lenteur de la connexion
                }
                
                // Fermer la connexion
                s1.close();
            } else {
                System.out.println("Demande inconnu reçue par le démon");
            }
            
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}


