package brahma.vmi.covid2019.database;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Login implements Serializable {    //介面為物件序列化,可將物件變成二進位串流,就可儲存成檔案,也可以直接將物件丟進輸出入串流做傳送
    private long id;
    private String loginType;
    private String username;
    private String password;
    private String ip;
    private String port;

    private String packageName;

    public Login() {
        loginType = "";
        username = "";
        password = "";
        ip = "";
        port = "";
        packageName = "";
    }

    public Login(long id, String loginType, String username, String password, String ip, String port, String packageName) {
        this.id = id;
        this.loginType = loginType;
        this.username = username;
        this.password = password;
        this.ip = ip;
        this.port = port;
        this.packageName = packageName;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLoginType() {
        return this.loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}