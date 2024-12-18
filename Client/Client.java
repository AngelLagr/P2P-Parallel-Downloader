package Client;

import Diary.DiaryRemote;

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

    public Client(int id, List<File> files) {
        this.id = id;
        try {
            this.deamon = new Deamon(this, files);
            // Connexion au Diary via RMI
            this.diary = (DiaryRemote) Naming.lookup("localhost/DiaryService");

            // Ajouter les fichiers du client au Diary
            for (File file : files) {
                diary.addFiles(file, this); // Enregistrer chaque fichier du client dans le Diary
            }

        } catch (Exception e) {
            System.out.println("Erreur lors de la connexion au Diary : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Client <id> <fichier1> <fichier2> ...");
            System.exit(1);
        }

        int id = Integer.parseInt(args[0]);
        List<File> files = new ArrayList<>();

        // Ajouter les fichiers fournis en argument
        for (int i = 1; i < args.length; i++) {
            File file = new File(args[i]);
            if (file.exists()) {
                files.add(file);
            } else {
                System.out.println("Le fichier " + args[i] + " n'existe pas.");
            }
        }

        // Lancer le client avec l'id et les fichiers
        Client client = new Client(id, files);
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
            System.out.println("Client démarré. Entrez une commande (getAnnuaire, getFichier <nom>, exit) :");

            while (true) {
                System.out.print("> ");
                String command = scanner.nextLine();

                if (command.equals("exit")) {
                    System.out.println("Arrêt du client.");
                    break;
                } else if (command.equals("getAnnuaire")) {
                    showAnnuaire();
                } else if (command.startsWith("getFichier ")) { //getfichier test.txt
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
                            completeFile.append(line);
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
}

  