package Diary;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Diary extends UnicastRemoteObject implements DiaryRemote {
    Map<String,List<Integer>> files;
    Map<String,Long> files_sizes;

    public Diary() throws RemoteException {
        super();
        this.files = new HashMap<>();
        this.files_sizes = new HashMap<>();
    }

    @Override
    public List<Integer> getClient(String file_name) {
        for (Map.Entry<String, List<Integer>> entry : files.entrySet()) {
            if (entry.getKey().equals(file_name)) {
                return entry.getValue();
            }
        }
        return new ArrayList<Integer>(); // Si vide
    }

    public List<String> getAllFiles() throws RemoteException{
        return new ArrayList<String>(files.keySet());
    }
    @Override
    public List<String> getFiles(Integer client) throws RemoteException{
        List<String> clientFiles = new ArrayList<String>();
        for (Map.Entry<String, List<Integer>> entry : files.entrySet()) {
            if (entry.getValue().contains(client)) {
                clientFiles.add(entry.getKey());
            }
        }
        return clientFiles;
    }
    
    @Override 
    public void addFiles(String fileName, Integer client, Long fileSize) throws RemoteException{
        if (files.containsKey(fileName)) {
            if (!files.get(fileName).contains(client)){
                files.get(fileName).add(client);
            } 
            files_sizes.put(fileName, fileSize);
        } else {
            List<Integer> clients = new ArrayList<Integer>();
            clients.add(client);
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