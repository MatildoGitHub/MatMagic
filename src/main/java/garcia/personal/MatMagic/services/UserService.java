package garcia.personal.MatMagic.services;

import garcia.personal.MatMagic.models.JwtAgpRequest;
import garcia.personal.MatMagic.models.User;
import garcia.personal.MatMagic.repositories.UserRepository;
import garcia.personal.MatMagic.utils.JwtUtils;
import garcia.personal.MatMagic.utils.PasswordEncoder;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtils jwtUtils;


    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User authenticate(String email, String password) {
        User user = findByEmail(email);
        if (user != null) {
            String hashedPassword = user.getPassword();
            return PasswordEncoder.matches(password, hashedPassword) ? user : null;
        }
        return null;
    }

    public List<User> listAll() {
        return userRepository.findAll();
    }

    public ResponseEntity<String> getUserResponseEntity(User user, BindingResult bindingResult) {

        ResponseEntity<String> hasErrors = isACorrectUserData(user, bindingResult);
        if (hasErrors != null) return hasErrors;

        // Verificar si el usuario ya existe en la base de datos
        Optional<User> existingUser = Optional.ofNullable(userRepository.findByEmail(user.getEmail()));
        if (existingUser.isPresent())
            return ResponseEntity.badRequest().build();


        // Crear el nuevo usuario
        user.setPassword(PasswordEncoder.encode(user.getPassword())); // Encriptar la contraseña
        User savedUser = userRepository.save(user);

        return ResponseEntity.created(URI.create("/api/users/" + savedUser.getId())).body(new StringBuilder().append("Email Send to email: ").append(user.getEmail()).toString());
    }

    public ResponseEntity<String> getLogUser(User user, BindingResult bindingResult) {

        ResponseEntity<String> hasErrors = isACorrectUserData(user, bindingResult);
        if (hasErrors != null) return hasErrors;

        User realUser = authenticate(user.getEmail(), user.getPassword());

        return realUser != null ?
                ResponseEntity.created(URI.create("/api/users/" + realUser.getId()))
                        .body(jwtUtils.generateJwt(realUser)) :
                ResponseEntity.badRequest().body("User data does not match");
    }


    public ResponseEntity<String> verifyJwt(JwtAgpRequest jwt, BindingResult bindingResult) {
        ResponseEntity<String> hasErrors = isACorrectJwtData(jwt, bindingResult);
        if (hasErrors != null) return hasErrors;

        Claims claim = jwtUtils.validateJwtAndGetClaims(jwt.getJwt()); //se verifica el jwt y devulve la claim
        if (claim == null || claim.getSubject().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("jwt provided is not valid");
        }
        try {
            User user = userRepository.findById(new Long(claim.getSubject().trim())).get();
            return ResponseEntity.badRequest().body("te has logeado con el email: " + user.getEmail() + "tu sesion expira en: " + claim.getExpiration());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("jwt provided is not valid");
        }
    }

    private static ResponseEntity<String> isACorrectUserData(User user, BindingResult bindingResult) {

        if (bindingResult.hasErrors() || user.getEmail().trim().isEmpty() || user.getPassword().trim().isEmpty())
            return ResponseEntity.badRequest().body("data provided has errors");

        return null;
    }

    private static ResponseEntity<String> isACorrectJwtData(JwtAgpRequest jwt, BindingResult bindingResult) {

        if (bindingResult.hasErrors() || jwt.getJwt().trim().isEmpty())
            return ResponseEntity.badRequest().body("data provided has errors");

        return null;
    }
}
