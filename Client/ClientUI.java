package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientUI {
    private Client client;

    public ClientUI(Client client) {
        this.client = client;
        initializeUI();
    }

    private void initializeUI() {
        // Créer la fenêtre principale
        JFrame frame = new JFrame("Client Interface");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        // Créer un panneau principal
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Zone de texte pour afficher les logs
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panneau pour les boutons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 3, 10, 10));

        // Bouton pour afficher l'annuaire
        JButton annuaireButton = new JButton("Afficher Annuaire");
        annuaireButton.addActionListener(e -> {
            try {
                textArea.append("Fichiers disponibles dans le Diary:\n");
                for (String file : this.client.diary.getAllFiles()) {
                    textArea.append("- " + file + "\n");
                }
            } catch (Exception ex) {
                textArea.append("Erreur lors de la récupération de l'annuaire : " + ex.getMessage() + "\n");
            }
        });
        buttonPanel.add(annuaireButton);

        JButton getFileButton = new JButton("Télécharger Fichier");
        getFileButton.addActionListener(e -> {
            try {
                // Récupérer la liste des fichiers disponibles dans le Diary
                String[] files = this.client.diary.getAllFiles().toArray(new String[0]);;
                
                // Créer une boîte de dialogue avec une liste déroulante des fichiers
                String fileName = (String) JOptionPane.showInputDialog(
                    frame, 
                    "Sélectionner un fichier à télécharger :", 
                    "Choisir un fichier", 
                    JOptionPane.PLAIN_MESSAGE, 
                    null, 
                    files, 
                    files.length > 0 ? files[0] : null // sélection par défaut
                );

                if (fileName != null && !fileName.trim().isEmpty()) {
                    try {
                        client.getFichier("getFichier " + fileName);
                        textArea.append("Fichier " + fileName + " téléchargé.\n");
                    } catch (Exception ex) {
                        textArea.append("Erreur lors du téléchargement : " + ex.getMessage() + "\n");
                    }
                }
            } catch (Exception ex) {
                textArea.append("Erreur lors de la récupération des fichiers : " + ex.getMessage() + "\n");
            }
        });
        buttonPanel.add(getFileButton);

        // Bouton pour ajouter un fichier
        JButton addFileButton = new JButton("Ajouter Fichier");
        addFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(frame);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                try {
                    client.addFichier(fileChooser.getSelectedFile().getAbsolutePath());
                    textArea.append("Fichier ajouté avec succès.\n");
                } catch (Exception ex) {
                    textArea.append("Erreur lors de l'ajout du fichier : " + ex.getMessage() + "\n");
                }
            }
        });
        buttonPanel.add(addFileButton);

        // Bouton pour ajouter un dossier
        JButton addFolderButton = new JButton("Ajouter Dossier");
        addFolderButton.addActionListener(e -> {
            JFileChooser folderChooser = new JFileChooser();
            folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnValue = folderChooser.showOpenDialog(frame);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                try {
                    client.addDossier(folderChooser.getSelectedFile().getAbsolutePath());
                    textArea.append("Dossier ajouté avec succès.\n");
                } catch (Exception ex) {
                    textArea.append("Erreur lors de l'ajout du dossier : " + ex.getMessage() + "\n");
                }
            }
        });
        buttonPanel.add(addFolderButton);

        

        // Bouton pour quitter
        JButton exitButton = new JButton("Quitter");
        exitButton.addActionListener(e -> System.exit(0));
        buttonPanel.add(exitButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ClientUI <id> <ip du Diary>");
            System.exit(1);
        }

        int id = Integer.parseInt(args[0]);
        String diaryIp = args[1];

        Client client = new Client(id, diaryIp);
        SwingUtilities.invokeLater(() -> new ClientUI(client));
    }
}
