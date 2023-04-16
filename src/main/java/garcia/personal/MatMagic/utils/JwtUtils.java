package garcia.personal.MatMagic.utils;

import garcia.personal.MatMagic.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtUtils {

    private static String SECRET_KEY;
    private static String FAKE_KEY = "El Betis Es El Mejor Equipo Del Mundo";

    @Autowired
    public JwtUtils(@Value("${secret.key}") String secretKey) {
        this.SECRET_KEY = secretKey; //basicamente esto es lo que hace que lea la clave secreta de secrets.properties
    }

    public String generateJwt(User user, boolean useRealKey) {

        // Se define la fecha de expiración (1 semana después de la creación del JWT)
        LocalDateTime expirationLocal = LocalDateTime.now().plusDays(7); // fecha de expiración: una semana a partir de la fecha actual
        Date expirationDate = Date.from(expirationLocal.atZone(ZoneId.systemDefault()).toInstant());

        // Se crea el JWT con los datos del usuario y la fecha de expiración
        return useRealKey ?
                Jwts.builder().setSubject(user.getId().toString()).setExpiration(expirationDate)
                        .signWith(SignatureAlgorithm.HS256, SECRET_KEY).compact() :
                Jwts.builder().setSubject(user.getId().toString()).setExpiration(expirationDate)
                        .signWith(SignatureAlgorithm.HS256, FAKE_KEY).compact();
    }

    public Claims validateJwtAndGetClaims(String jwt, boolean useRealKey) {
        try {
            Claims claims = useRealKey ? Jwts.parser().setSigningKey(SECRET_KEY)
                    .parseClaimsJws(jwt).getBody() :
                    Jwts.parser().setSigningKey(FAKE_KEY)
                            .parseClaimsJws(jwt).getBody();
            return claims;
        } catch (Exception e) {
            // en caso de que haya un error al validar el JWT
            // se puede lanzar una excepción personalizada o simplemente retornar null
            return null;
        }
    }

}
