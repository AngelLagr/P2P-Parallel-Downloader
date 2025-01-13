package Client;

import Diary.DiaryRemote;
import Downloader.Downloader;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Map;

public class Client implements Runnable, Serializable {
    private int id;
    private String ip;
    public DiaryRemote diary; // Référence RMI vers le Diary
    private Deamon deamon;
    private Downloader downloader;
    private String diary_ip;

    public Client(int id, String diary_ip) {
        this.id = id;
        this.diary_ip = diary_ip;
        this.ip = getIp();
        try {
            this.deamon = new Deamon(this);
            this.downloader = new Downloader(this);

            // Connexion au Diary via RMI
            Registry reg = LocateRegistry.getRegistry(this.diary_ip, 1099);
            this.diary = (DiaryRemote) reg.lookup("DiaryService");

        } catch (Exception e) {
            System.out.println("Erreur lors de la connexion au Diary : " + e.getMessage());
        }
    }

    public String getIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();

        } catch (Exception e) {
            return "Erreur dans la récupération de l'ip";
        }
    }

    public String getDiaryIp() {
        return this.diary_ip;
    }

    public Deamon getDeamon() {
        return this.deamon;
    }

    public int getId() {
        return this.id;
    }

    public void run() {
        // Placeholder method for future integration
    }

    /**
     * Affiche l'annuaire des fichiers disponibles.
     */
    public String showAnnuaire() {
        StringBuilder result = new StringBuilder();
        try {
            result.append("Fichiers disponibles dans le Diary :\n");
            for (String file : diary.getAllFiles()) {
                result.append("- ").append(file).append(" de taille ").append(diary.getFileSizeDiary(file))
                      .append(" détenu par : ").append(diary.getClient(file)).append("\n");
            }
        } catch (Exception e) {
            return "Erreur lors de la récupération de l'annuaire : " + e.getMessage();
        }
        return result.toString();
    }

    public String getFichier(String command) {
        String file_name = command.substring(11).trim();

        byte[] completeFile;
        try {
            completeFile = downloader.download(file_name);

            // Créer un fichier local et y écrire le contenu téléchargé
            File newFile = new File("./downloads/" + file_name);
            try (FileOutputStream fileOutputStream = new FileOutputStream(newFile)) {
                fileOutputStream.write(completeFile);
                return "Fichier téléchargé et enregistré sous : " + newFile.getAbsolutePath();
            } catch (Exception e) {
                return "Erreur lors de l'enregistrement du fichier : " + e.getMessage();
            }
        } catch (Exception e) {
            return "Erreur de connexion lors du téléchargement : " + e.getMessage();
        }
    }

    // Cette methode permet au client de notifier qu'il possède un fichier
    public String addFichier(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                try {
                    this.diary.addFiles(file.getName(), this.ip, this.deamon.getPort(), file.length());
                    this.deamon.addFile(file);
                    return "Fichier ajouté avec succès : " + filePath;
                } catch (Exception e) {
                    return "Erreur lors de l'ajout du fichier avec le démon : " + e.getMessage();
                }
            } else {
                return "Il n'y a pas de fichier au chemin : " + filePath;
            }
        } catch (Exception e) {
            return "Remote Exception : " + e.getMessage();
        }
    }

    public String addDossier(String filePath) {
        File dossier = new File(filePath);
        if (!dossier.exists()) {
            return "Il n'y a pas de dossier ou de fichier au chemin : " + filePath;
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
            return "Dossier ajouté avec succès : " + filePath;
        } else {
            return "Le chemin spécifié n'est pas un dossier valide.";
        }
    }

    

    public String print(String texte) {
        return texte;
    }
}
