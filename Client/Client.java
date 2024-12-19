package Client;

import Diary.DiaryRemote;

import java.rmi.RemoteException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client implements Runnable,Serializable {
    private int id;
    private DiaryRemote diary; // Référence RMI vers le Diary
    private Deamon deamon;

    public Client(int id) {
        this.id = id;
        try {
            this.deamon = new Deamon(this);

            // Connexion au Diary via RMI
            this.diary = (DiaryRemote) Naming.lookup("localhost/DiaryService");

        } catch (Exception e) {
            System.out.println("Erreur lors de la connexion au Diary : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Client <id>");
            System.exit(1);
        }

        int id = Integer.parseInt(args[0]);

        // Lancer le client avec l'id et les fichiers
        Client client = new Client(id);
        new Thread(client).start();
    }


    public Deamon getDeamon(){
        return deamon;
    } 

    public int getId() {
        return id;
    }

    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Client démarré. Entrez une commande (getAnnuaire, getFichier <nom>, addFichier <nom>, addDossier <nom>, exit) :");

            while (true) {
                System.out.print("> ");
                String command = scanner.nextLine();

                if (command.equals("exit")) {
                    System.out.println("Arrêt du client.");
                    break;
                } else if (command.equals("getAnnuaire")) {
                    showAnnuaire();
                } else if (command.startsWith("getFichier ")) { //getfichier test.txt
                    getFichier(command);
                } else if (command.startsWith("addFichier")) {
                    String filePath = command.substring(11).trim();
                    addFichier(filePath);
                } else if (command.startsWith("addDossier")) {
                    String filePath = command.substring(11).trim();
                    addDossier(filePath);
                } else {
                    System.out.println("Commande non reconnue.");
                }
            }
        }
    }   

    /**
     * Affiche l'annuaire des fichiers disponibles.
     */
    private void showAnnuaire() {
        try {
            System.out.println("Fichiers disponibles dans le Diary :");
            for (File file : diary.getAllFiles()) {
                System.out.println("- " + file.getName() + " détenu par : "+ diary.getClient(file.getName()));
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération de l'annuaire : " + e.getMessage());
        }
    }

    private void getFichier(String command) {
        String fileName = command.substring(11).trim();
                    
        // Créer une connexion avec le serveur
        try (Socket socket = new Socket("localhost", 8080 );
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Envoyer la requête GET au serveur
            out.println("GET " + fileName);

            // Lire la réponse du serveur (le fichier reconstruit)
            StringBuilder completeFile = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                completeFile.append(line + "\n");
            }
            // Créer un fichier local et y écrire le contenu téléchargé
            File newFile = new File("./downloads/" + fileName);
            try (PrintWriter writer = new PrintWriter(newFile)) {
                writer.write(completeFile.toString());
                System.out.println("Fichier téléchargé et enregistré sous : " + newFile.getAbsolutePath());
            } catch (Exception e) {
                System.out.println("Erreur lors de l'enregistrement du fichier : " + e.getMessage());
            }
        } catch (IOException e) {
            System.out.println("Erreur de connexion ou de téléchargement : " + e.getClass() +e.getMessage());
        }
    }

    // Cette methode permet au client de notifier qu'il possède un fichier
    private void addFichier(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                this.diary.addFiles(file, this);
                this.deamon.files.add(file);
            } else {
                System.out.println("Il n'y a pas de fichier à l'adresse : " + filePath);
            }  
        } catch (RemoteException e) {
            System.out.println("Remote Exception" + e.getMessage());
        }
        
    }

    private void addDossier(String filePath) {
        File dossier = new File(filePath);
        if (!dossier.exists()) {
            System.out.println("Il n'y a pas de dossier ou de fichier à l'adresse : " + filePath);
        }
        if (dossier.isDirectory()) {
            File[] fichiers = dossier.listFiles();
            if (fichiers != null) {
                for (File fichier : fichiers) {
                    if (!fichier.isDirectory()) {
                        addFichier(fichier.getAbsolutePath());
                    } else {
                        addDossier(fichier.getAbsolutePath());
                    }
                }
            }
        } else {
            System.out.println("Le chemin spécifié n'est pas un dossier valide.");
        }
    }
}

  