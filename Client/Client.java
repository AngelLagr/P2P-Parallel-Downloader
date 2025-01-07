package Client;

import Diary.DiaryRemote;
import Downloader.Downloader;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Scanner;

public class Client implements Runnable,Serializable {
    private int id;
    private DiaryRemote diary; // Référence RMI vers le Diary
    private Deamon deamon;
    private Downloader downloader;

    public Client(int id) {
        this.id = id;
        try {
            this.deamon = new Deamon(this);
            this.downloader = new Downloader(this);
            // Connexion au Diary via RMI
            Registry reg = LocateRegistry.getRegistry("localhost",1099);
            this.diary = (DiaryRemote) reg.lookup("DiaryService");

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
        return this.deamon;
    } 

    public int getId() {
        return this.id;
    }

    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Client démarré. Entrez une commande (getAnnuaire, getFichier <nom>, addFichier <chemin>, addDossier <chemin>, reload, exit) :");

            while (true) {
                System.out.print("> ");
                String command = scanner.nextLine();

                if (command.equals("exit")) {
                    System.out.println("Arrêt du client.");
                    break;
                } else if (command.equals("getAnnuaire")) {
                    showAnnuaire();
                } else if (command.startsWith("getFichier ")) { //getfichier test.txt
                    long startTime = System.currentTimeMillis();

                    getFichier(command);
                    long endTime = System.currentTimeMillis();
                    System.out.println("\nDurée du téléchargement : " + (endTime-startTime) +"ms");
                } else if (command.startsWith("addFichier")) {
                    String filePath = command.substring(11).trim();
                    addFichier(filePath);
                } else if (command.startsWith("reload")) {
                    reloadFiles();
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
            for (String file : diary.getAllFiles()) {
                System.out.println("- " + file + " de taille " + diary.getFileSizeDiary(file) + " détenu par : "+ diary.getClient(file));
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération de l'annuaire : " + e.getMessage());
        }
    }

    private void getFichier(String command) {
        String file_name = command.substring(11).trim();

        byte[] completeFile;
        try {
            completeFile = downloader.download(file_name);
            
            // Créer un fichier local et y écrire le contenu téléchargé
            File newFile = new File("./downloads/" + file_name);
            try (FileOutputStream fileOutputStream = new FileOutputStream(newFile)) {
                fileOutputStream.write(completeFile);
                System.out.println("Fichier téléchargé et enregistré sous : " + newFile.getAbsolutePath());
            } catch (Exception e) {
                System.out.println("Erreur lors de l'enregistrement du fichier : " + e.getMessage());
            }
        } catch (Exception e){
            System.out.println("Erreur de connexion lors du téléchargement : " + e.getMessage());
        }
    }

    // Cette methode permet au client de notifier qu'il possède un fichier
    private void addFichier(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                try {
                    this.diary.addFiles(file.getName(), this.deamon.getPort(), file.length());
                    this.deamon.addFile(file);
                } catch(Exception e) {
                    System.out.println("Erreur lors de la récupération du nombre de ligne d'une fichier avec le demon : " + e.getMessage());
                }
            } else {
                System.out.println("Il n'y a pas de fichier au chemin : " + filePath);
            }  
        } catch (Exception e) {
            System.out.println("Remote Exception" + e.getMessage());
        }
        
    }

    private void addDossier(String filePath) {
        File dossier = new File(filePath);
        if (!dossier.exists()) {
            System.out.println("Il n'y a pas de dossier ou de fichier au chemin : " + filePath);
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

    public void reloadFiles() {
        for (Map.Entry<String,File> entry: deamon.files.entrySet()) {
            try { 
                diary.addFiles(entry.getValue().getName(),this.deamon.getPort(), entry.getValue().length());
            } catch (Exception e) {
                System.out.println("Erreur dans le rechargement des fichiers" + e);
            }
        }
    } 

    public void print(String texte) {
        System.out.println(texte);
    }
}

  