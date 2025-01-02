package Diary;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class DiaryServer {
    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            DiaryRemote diary = new Diary();
            Naming.rebind("localhost/DiaryService", diary);
            System.out.println("DiaryService is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}