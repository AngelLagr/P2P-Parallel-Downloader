package Diary;

import Client.Client;
import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface DiaryRemote extends Remote {
    List<Client> getClient(String file_name) throws RemoteException;
    List<String> getFiles(Client client) throws RemoteException;
    void addFiles(String file, Client client) throws RemoteException;
    void removeFiles(String name_file) throws RemoteException;
    List<String> getAllFiles()throws RemoteException;
}
