package Diary;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Diary extends UnicastRemoteObject implements DiaryRemote {
    Map<String,List<ClientRepresentation>> files; // {nom_du_fichier : (IP1, port1),(IP2,port2)... ; ...}
    Map<String,Long> files_sizes; // {nom_du_fichier : taille du fichier en octets}

    public Diary() throws RemoteException {
        super();
        this.files = new HashMap<>();
        this.files_sizes = new HashMap<>();
    }

    @Override
    public List<ClientRepresentation> getClient(String file_name) {
        for (Map.Entry<String,List<ClientRepresentation>> entry : files.entrySet()) {
            if (entry.getKey().equals(file_name)) {
                return entry.getValue();
            }
        }
        return new ArrayList<ClientRepresentation>(); // Si vide
    }

    public List<String> getAllFiles() throws RemoteException{
        return new ArrayList<String>(files.keySet());
    }
    
    @Override
    public List<String> getFiles(String IPClient, Integer port) throws RemoteException {
        List<String> clientFiles = new ArrayList<String>();
        for (Map.Entry<String, List<ClientRepresentation>> entry : files.entrySet()) {
            for (ClientRepresentation element : entry.getValue())
                if (element.getAdresse() == IPClient && element.getPort() == port) {
                    clientFiles.add(entry.getKey());
                }
            }
        return clientFiles;
    }
    
    @Override 
    public void addFiles(String fileName, String IPClient, Integer port, Long fileSize) throws RemoteException{
        if (files.containsKey(fileName)) {
            Boolean isInside = false;
            for (ClientRepresentation element : this.files.get(fileName)){
                if (element.getAdresse() == IPClient && element.getPort() == port) {
                    isInside = true;
                } 
            }
            if (!(isInside)) {files.get(fileName).add(new ClientRepresentation(IPClient, port));}
            files_sizes.put(fileName, fileSize);
        } else {
            List<ClientRepresentation> clients = new ArrayList<ClientRepresentation>();
            clients.add(new ClientRepresentation(IPClient, port));
            files.put(fileName, clients);
            files_sizes.put(fileName,fileSize);
        }
    }

    @Override
    public Long getFileSizeDiary(String fileName) throws RemoteException{
        if (files_sizes.containsKey(fileName)) {
            return files_sizes.get(fileName);
        }
        else {
            return null;
        }
    }
    
    @Override
    public void removeFiles(String name_file) {
        files.entrySet().removeIf(entry -> entry.getKey().equals(name_file));
    }   
}
//on peut faire reload pour reloder la size des fichiers