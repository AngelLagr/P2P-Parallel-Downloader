package Diary;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface DiaryRemote extends Remote {
    List<ClientRepresentation> getClient(String file_name) throws RemoteException;
    List<String> getFiles(String IPClient, Integer port) throws RemoteException;
    void addFiles(String fileName, String client_ip, Integer client, Long fileSize) throws RemoteException;
    void removeFiles(String name_file) throws RemoteException;
    List<String> getAllFiles()throws RemoteException;
    Long getFileSizeDiary(String fileName)throws RemoteException;
    void removeClients(ClientRepresentation client) throws RemoteException;
}
