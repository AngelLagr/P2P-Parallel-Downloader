package Downloader;

public class ExceptionPlusDeClient extends Throwable {
    public ExceptionPlusDeClient() {
        super("Tout les clients associés au fichier demandé sont déconnectés.");
    }

    public ExceptionPlusDeClient(String message) {
        super(message);
    }
}
