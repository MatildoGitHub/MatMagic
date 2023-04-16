package garcia.personal.MatMagic.utils;

import garcia.personal.MatMagic.models.User;

import java.time.LocalDate;
import java.util.Date;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {
    @Value("${secret.key}")
    private static String SECRET_KEY;

    @Autowired
    public JwtUtils(@Value("${secret.key}") String secretKey) {
        this.SECRET_KEY = secretKey;
    }
    public String generateJwt(User user) {

        // Se define la fecha de expiración (1 semana después de la creación del JWT)
        LocalDate expirationLocal = LocalDate.now().plusDays(7); // fecha de expiración: una semana a partir de la fecha actual
        Date expirationDate = java.sql.Date.valueOf(expirationLocal);

        // Se crea el JWT con los datos del usuario y la fecha de expiración
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId())
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
    public  Claims validateJwtAndGetClaims(String jwt) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(jwt)
                    .getBody();
            return claims;
        } catch (Exception e) {
            // en caso de que haya un error al validar el JWT
            // se puede lanzar una excepción personalizada o simplemente retornar null
            return null;
        }
    }

}
