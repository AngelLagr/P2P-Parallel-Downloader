package Diary;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class DiaryServer {
    public static void main(String[] args) {
        try {
            Registry reg = LocateRegistry.createRegistry(1099);
            DiaryRemote diary = new Diary();
            reg.rebind("DiaryService", diary);
            System.out.println("DiaryService is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}