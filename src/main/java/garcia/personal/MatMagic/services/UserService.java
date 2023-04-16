package garcia.personal.MatMagic.services;

import garcia.personal.MatMagic.models.JwtAgpRequest;
import garcia.personal.MatMagic.models.User;
import garcia.personal.MatMagic.repositories.UserRepository;
import garcia.personal.MatMagic.utils.FinalUtil;
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

    //funcion para detectar si los datos de inicio de sesion son correctos
    public User authenticate(String email, String password) {
        User user = findByEmail(email);
        if (user != null) {
            String hashedPassword = user.getPassword();
            return PasswordEncoder.matches(password, hashedPassword) ? user : null;
        }
        return null;
    }

    //funcion para listar todos los usuarios
    public List<User> listAll() {
        return userRepository.findAll();
    }

    //esta funcion sirve para crear Un Usuario
    public ResponseEntity<String> getUserCreated(User user, BindingResult bindingResult) {

        ResponseEntity<String> hasErrors = isACorrectUserData(user, bindingResult);
        if (hasErrors != null) return hasErrors;

        // Verificar si el usuario ya existe en la base de datos
        Optional<User> existingUser = Optional.ofNullable(userRepository.findByEmail(user.getEmail()));
        if (existingUser.isPresent())
            return ResponseEntity.badRequest().body(FinalUtil.USER_ALREADY_EXIST);


        // Crear el nuevo usuario
        user.setPassword(PasswordEncoder.encode(user.getPassword())); // Encriptar la contraseña
        User savedUser = userRepository.save(user);

        return ResponseEntity.created(URI.create(FinalUtil.PATH_CREATE)).body(new StringBuilder().append(FinalUtil.EMAIL_SEND_TO).append(savedUser.getEmail()).toString());
    }

    //funcion para logear y devolver el jwt
    public ResponseEntity<String> getLogUser(User user, BindingResult bindingResult) {

        ResponseEntity<String> hasErrors = isACorrectUserData(user, bindingResult);
        if (hasErrors != null) return hasErrors;

        User realUser = authenticate(user.getEmail(), user.getPassword());

        return realUser != null ? ResponseEntity.created(URI.create(FinalUtil.PATH_LOG + realUser.getId())).body(jwtUtils.generateJwt(realUser, true)) : ResponseEntity.badRequest().body(FinalUtil.USER_DATA_DOES_NOT_MATCH);
    }

    //funcion para verificar que el jwt es valido
    public ResponseEntity<String> verifyJwt(JwtAgpRequest jwt, BindingResult bindingResult) {
        ResponseEntity<String> hasErrors = isACorrectJwtData(jwt, bindingResult);
        if (hasErrors != null) return hasErrors;

        Claims claim = jwtUtils.validateJwtAndGetClaims(jwt.getJwt(), true); //se verifica el jwt y devulve la claim
        if (claim == null || claim.getSubject().trim().isEmpty())
            return ResponseEntity.badRequest().body(FinalUtil.JWT_PROVIDED_IS_NOT_VALID);

        try {
            User user = userRepository.findById(new Long(claim.getSubject().trim())).get();
            return ResponseEntity.ok().body(FinalUtil.TE_HAS_LOGEADO_CON_EL_EMAIL + user.getEmail() + FinalUtil.TU_SESION_EXPIRA_EN + claim.getExpiration());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(FinalUtil.JWT_PROVIDED_IS_NOT_VALID);
        }
    }

    //funcion para comprobar que los datos del usuario enviado son validos
    private static ResponseEntity<String> isACorrectUserData(User user, BindingResult bindingResult) {

        if (bindingResult.hasErrors() || user.getEmail().trim().isEmpty() || user.getPassword().trim().isEmpty())
            return ResponseEntity.badRequest().body(FinalUtil.DATA_PROVIDED_HAS_ERRORS);

        return null;
    }

    //funcion para comprobar que los datos del jwt enviado son correctos
    private static ResponseEntity<String> isACorrectJwtData(JwtAgpRequest jwt, BindingResult bindingResult) {

        if (bindingResult.hasErrors() || jwt.getJwt().trim().isEmpty())
            return ResponseEntity.badRequest().body(FinalUtil.DATA_PROVIDED_HAS_ERRORS);

        return null;
    }
}
