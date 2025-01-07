package Diary;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface DiaryRemote extends Remote {
    List<Integer> getClient(String file_name) throws RemoteException;
    List<String> getFiles(Integer client) throws RemoteException;
    void addFiles(String fileName, Integer client, Long fileSize) throws RemoteException;
    void removeFiles(String name_file) throws RemoteException;
    List<String> getAllFiles()throws RemoteException;
    Long getFileSizeDiary(String fileName)throws RemoteException;
}
