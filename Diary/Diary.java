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
    Map<String,List<Integer>> files;

    public Diary() throws RemoteException {
        super();
        this.files = new HashMap<>();
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
    public void addFiles(String fileName, Integer client) {
        if (files.containsKey(fileName)) {
            files.get(fileName).add(client);
        }
        else {
            List<Integer> clients = new ArrayList<Integer>();
            clients.add(client);
            files.put(fileName, clients);
        }
    }
    
    @Override
    public void removeFiles(String name_file) {
        files.entrySet().removeIf(entry -> entry.getKey().equals(name_file));
    }

    
}
