package Client;

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

public class Deamon implements Serializable {
    Client client;
    Map<String, File> files;
    int port;

    public Deamon(Client client) {
        this.client = client;
        this.files = new HashMap<String, File>();
        this.port = 8080 + client.getId();
        startClientServer();
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
        return result;
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

    public Slave (Socket s, Deamon deamon) {
        this.s1 =s;
        this.deamon = deamon;
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
                byte[] filePart = this.deamon.getFilePart(filePath, start, end);

                // Envoyer la réponse au downloader
                s1_out.write(filePart);
                s1_out.flush();

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


