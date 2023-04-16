package garcia.personal.MatMagic.models;

public class JwtAgpRequest {
    private String jwt;
    public JwtAgpRequest() {
    }
    public JwtAgpRequest(String jwt) {
        this.jwt = jwt;
    }
    public String getJwt() {
        return jwt;
    }
    public void setJwt(String jwt) {
        this.jwt = jwt;
    }
}
