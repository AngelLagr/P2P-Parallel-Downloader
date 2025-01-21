package Downloader;

public class ExceptionFichierVide extends Throwable {
    public ExceptionFichierVide() {
        super("Le fichier est vide.");
    }

    public ExceptionFichierVide(String message) {
        super(message);
    }
}
