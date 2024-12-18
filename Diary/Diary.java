package Diary;
import Client.Client;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Diary extends UnicastRemoteObject implements DiaryRemote {
    Map<File,List<Client>> files;

    public Diary() throws RemoteException {
        super();
        this.files = new HashMap<>();
    }

    @Override
    // quels client sont associés a ce fichier
    public List<Client> getClient(String file_name) {
        for (Map.Entry<File, List<Client>> entry : files.entrySet()) {
            if (entry.getKey().getName().equals(file_name)) {
                return entry.getValue();
            }
        }
        return new ArrayList<Client>(); // Si vide
    }

    public List<File> getAllFiles() throws RemoteException{
        return new ArrayList<File>(files.keySet());
    }
    @Override
    // quels fichiers sont associés a ce client
    public List<File> getFiles(Client client) throws RemoteException{
        List<File> clientFiles = new ArrayList<File>();
        for (Map.Entry<File, List<Client>> entry : files.entrySet()) {
            if (entry.getValue().contains(client)) {
                clientFiles.add(entry.getKey());
            }
        }
        return clientFiles;
    }
    
    @Override 
    public void addFiles(File file, Client client) {
        if (files.containsKey(file)) {
            files.get(file).add(client);
        }
        else {
            List<Client> clients = new ArrayList<Client>();
            clients.add(client);
            files.put(file, clients);
        }
    }
    
    @Override
    public void removeFiles(String name_file) {
        files.entrySet().removeIf(entry -> entry.getKey().getName().equals(name_file));
    }

    public int getSize(String file_name){
        for (Map.Entry<File, List<Client>> entry : files.entrySet()) {
            if (entry.getKey().getName().equals(file_name)) {
                try {
                    return countLines(entry.getKey().getName());
                } catch (FileNotFoundException e) {
                    return -1;
                }

            }
        }
        return -2;
    }

    public File getFileFromName(String fileName) {
        // Recherche du premier File correspondant au nom file_names
        File return_file = null;
        for (Map.Entry<File, List<Client>> entry : this.files.entrySet()) {
            File file = entry.getKey();
            if (file.getName().equals(fileName)) {
                return_file = file;
                break;  // Sortir de la boucle dès qu'on a trouvé le premier fichier
            }
        }
        return return_file;
    }

    public int countLines(String fileName) throws FileNotFoundException{
        int lineCount = 0;
        System.out.println(lineCount)   ;
        System.out.println(fileName)   ;

        File file = getFileFromName(fileName);
        if (file == null) {throw new FileNotFoundException();}
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineCount++;
                System.out.println("Ligne lue: " + line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(lineCount)   ; 
        return lineCount;
    }
}
