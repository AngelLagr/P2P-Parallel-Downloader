package Client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Deamon implements Serializable {
    Client client;
    List<File> files;
    int port;

    public Deamon(Client client, List<File> files) {
        this.client = client;
        this.files = files;
        this.port = 8080 + client.getId();
        startClientServer();
    }

    public int getPort(){
        return port;
    }

    // J'obtiens une partie du fichier en fonction de comment je le partitionne
    public String getFilePart(String filepath, int start, int end) throws IOException {
    StringBuilder result = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
        int currentLine = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            if (currentLine >= start && currentLine < end) {
                result.append(line).append("\n");
            }
            currentLine++;
            if (currentLine >= end) break;
        }
    }
    
    return result.toString();
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
    private void addFile(File file) {
        this.files.add(file);
        
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

            // La requête contient : "filename,start,end"
            String[] parts = request.split(",");
            String fileName = parts[0];
            int start = Integer.parseInt(parts[1]);
            int end = Integer.parseInt(parts[2]);
            // Lire la partie demandée du fichier
            String filePath = this.deamon.files.stream()
                              .filter(file -> file.getName().equals(fileName))
                              .findFirst()
                              .orElseThrow(() -> new IOException("File not found"))
                              .getAbsolutePath();
            String filePart = this.deamon.getFilePart(filePath, start, end);

            // Envoyer la réponse au downloader
            s1_out.write(filePart.getBytes());
            s1_out.flush();

            // Fermer la connexion
            s1.close();

        } catch (IOException e) {
            System.out.println(e);
        }
    }
}


