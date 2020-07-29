package brahma.vmi.brahmalibrary.database;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Item implements Serializable {    //介面為物件序列化,可將物件變成二進位串流,就可儲存成檔案,也可以直接將物件丟進輸出入串流做傳送
    private long id;
    private String isUsingBio;
    private String bioUsername;
    private String bioPassword;
    private String bioIP;
    private String bioPort;

    public Item() {
        isUsingBio = "false";
        bioUsername = "";
        bioPassword = "";
        bioIP = "";
        bioPort = "";
    }

    public Item(long id, String isUsingBio, String bioUsername, String bioPassword, String bioIP, String bioPort) {
        this.id = id;
        this.isUsingBio = isUsingBio;
        this.bioUsername = bioUsername;
        this.bioPassword = bioPassword;
        this.bioIP = bioIP;
        this.bioPort = bioPort;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    public void setUsingBio(String isUsingBio) {
        this.isUsingBio = isUsingBio;
    }

    public String getUsingBio() {
        return this.isUsingBio;
    }

    public void setbioUsername(String bioUsername) {
        this.bioUsername = bioUsername;
    }

    public String getbioUsername() {
        return this.bioUsername;
    }

    public void setbioPassword(String bioPassword) {
        this.bioPassword = bioPassword;
    }

    public String getbioPassword() {
        return this.bioPassword;
    }

    public void setbioIP(String bioIP) {
        this.bioIP = bioIP;
    }

    public String getbioIP() {
        return this.bioIP;
    }

    public void setbioPort(String bioPort) {
        this.bioPort = bioPort;
    }

    public String getbioPort() {
        return this.bioPort;
    }
}