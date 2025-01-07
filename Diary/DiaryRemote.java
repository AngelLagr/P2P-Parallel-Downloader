package Diary;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface DiaryRemote extends Remote {
    List<Integer> getClient(String file_name) throws RemoteException;
    List<String> getFiles(Integer client) throws RemoteException;
    void addFiles(String file, Integer client) throws RemoteException;
    void removeFiles(String name_file) throws RemoteException;
    List<String> getAllFiles()throws RemoteException;
}
