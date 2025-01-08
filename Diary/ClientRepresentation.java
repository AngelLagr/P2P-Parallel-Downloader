package Diary;
public class ClientRepresentation{
    private String adresse;
    private Integer port;

    public ClientRepresentation(String adresse, Integer port) {
        this.adresse = adresse;
        this.port = port;
    }

    public String getAdresse() {
        return this.adresse;
    }

    public Integer getPort() {
        return this.port;
    }

}