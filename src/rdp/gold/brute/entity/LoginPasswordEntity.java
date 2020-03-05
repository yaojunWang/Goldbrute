package rdp.gold.brute.entity;

import java.util.Scanner;

public class LoginPasswordEntity {
    private String login;
    private String password;

    public LoginPasswordEntity() {
    }

    public LoginPasswordEntity(String login, String password) {
        this.login = login;
        this.password = password;
    }

    @SuppressWarnings("resource")
    public static LoginPasswordEntity parseLoginPasswordEntity(String loginPassword) throws Exception {
        if (!loginPassword.contains(":")) {
            throw new Exception(loginPassword + " is not login password");
        }

        Scanner scanner = new Scanner(loginPassword).useDelimiter(":");

        LoginPasswordEntity loginPasswordEntity = new LoginPasswordEntity();
        loginPasswordEntity.setLogin(scanner.next());

        if (scanner.hasNext()) {
            loginPasswordEntity.setPassword(scanner.next());
        } else {
            loginPasswordEntity.setPassword("");
        }

        return loginPasswordEntity;
    }

    public String getLogin() {
        return this.login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String toString() {
        return this.login + ":" + this.password;
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if ((o == null) || (getClass() != o.getClass()))
            return false;
        LoginPasswordEntity that = (LoginPasswordEntity) o;
        return (java.util.Objects.equals(this.login, that.login)) && (java.util.Objects.equals(this.password, that.password));
    }

    public int hashCode() {
        return java.util.Objects.hash(new Object[] { this.login, this.password });
    }
}
