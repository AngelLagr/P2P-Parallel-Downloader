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
    Map<String,List<Client>> files;

    public Diary() throws RemoteException {
        super();
        this.files = new HashMap<>();
    }

    @Override
    public List<Client> getClient(String file_name) {
        for (Map.Entry<String, List<Client>> entry : files.entrySet()) {
            if (entry.getKey().equals(file_name)) {
                return entry.getValue();
            }
        }
        return new ArrayList<Client>(); // Si vide
    }

    public List<String> getAllFiles() throws RemoteException{
        return new ArrayList<String>(files.keySet());
    }
    @Override
    public List<String> getFiles(Client client) throws RemoteException{
        List<String> clientFiles = new ArrayList<String>();
        for (Map.Entry<String, List<Client>> entry : files.entrySet()) {
            if (entry.getValue().contains(client)) {
                clientFiles.add(entry.getKey());
            }
        }
        return clientFiles;
    }
    
    @Override 
    public void addFiles(String fileName, Client client) {
        if (files.containsKey(fileName)) {
            files.get(fileName).add(client);
        }
        else {
            List<Client> clients = new ArrayList<Client>();
            clients.add(client);
            files.put(fileName, clients);
        }
    }
    
    @Override
    public void removeFiles(String name_file) {
        files.entrySet().removeIf(entry -> entry.getKey().equals(name_file));
    }

    
}
