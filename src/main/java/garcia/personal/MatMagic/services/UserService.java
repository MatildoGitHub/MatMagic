package garcia.personal.MatMagic.services;

import garcia.personal.MatMagic.models.JsonUUID;
import garcia.personal.MatMagic.models.JwtAgpRequest;
import garcia.personal.MatMagic.models.User;
import garcia.personal.MatMagic.repositories.UserRepository;
import garcia.personal.MatMagic.utils.FinalUtil;
import garcia.personal.MatMagic.utils.JwtUtils;
import garcia.personal.MatMagic.utils.PasswordEncoder;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import javax.mail.internet.MimeMessage;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private JavaMailSender javaMailSender;

    //funcion para detectar si los datos de inicio de sesion son correctos
    // function to check if login credentials are correct
    public User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            String hashedPassword = user.getPassword();
            return PasswordEncoder.matches(password, hashedPassword) ? user : null;
        }
        return null;
    }

    //funcion para listar todos los usuarios
    // Function to list all users
    public List<User> listAll() {
        return userRepository.findAll();
    }

    //esta funcion sirve para crear un Usuario
    // This function is used to create a user
    public ResponseEntity<String> getUserCreated(User user, BindingResult bindingResult) {

        ResponseEntity<String> hasErrors = isACorrectUserData(user, bindingResult);
        if (hasErrors != null) return hasErrors;

        // Verifica si el usuario ya existe en la base de datos
        // Verifies if the user already exists in the database
        Optional<User> existingUser = Optional.ofNullable(userRepository.findByEmail(user.getEmail()));
        if (existingUser.isPresent())
            return ResponseEntity.badRequest().body(FinalUtil.USER_ALREADY_EXIST);

        // Crear el nuevo usuario
        // Create the new user.
        user.setPassword(PasswordEncoder.encode(user.getPassword())); // Encriptar la contraseña
        // Genera uuid y lo desactiva para que no pueda logearse
        // Generates a UUID and deactivates it so that it cannot log in.
        user.setUuid(UUID.randomUUID().toString());
        user.setActive(false);
        if (sendMail(user)) {
            User savedUser = userRepository.save(user);
            return ResponseEntity.created(URI.create(FinalUtil.PATH_CREATE)).body(new StringBuilder().append(FinalUtil.EMAIL_SEND_TO).append(savedUser.getEmail()).toString());
        }

        return ResponseEntity.badRequest().body(FinalUtil.EMAIL_WENT_WRONG);
    }

    //funcion para logear y devolver el jwt
    // Function for logging in and returning JWT token
    public ResponseEntity<String> getLogUser(User user, BindingResult bindingResult) {

        ResponseEntity<String> hasErrors = isACorrectUserData(user, bindingResult);
        if (hasErrors != null) return hasErrors;

        User realUser = authenticate(user.getEmail(), user.getPassword());
        if (!realUser.getActive())
            return ResponseEntity.badRequest().body(FinalUtil.VERIFY_ACCOUNT_EMAIL);

        return realUser != null ? ResponseEntity.created(URI.create(FinalUtil.PATH_LOG + realUser.getId())).body(jwtUtils.generateJwt(realUser, true))
                : ResponseEntity.badRequest().body(FinalUtil.USER_DATA_DOES_NOT_MATCH);
    }

    //funcion para verificar que el jwt es valido
    // Function to verify that the JWT is valid
    public ResponseEntity<String> verifyJwt(JwtAgpRequest jwt, BindingResult bindingResult) {

        ResponseEntity<String> hasErrors = isACorrectStringData(jwt.getJwt(), bindingResult);
        if (hasErrors != null) return hasErrors;


        //se verifica el jwt y devulve las claims
        // Verify the JWT and return the claims
        Claims claim = jwtUtils.validateJwtAndGetClaims(jwt.getJwt(), true);
        if (claim == null || claim.getSubject().trim().isEmpty())
            return ResponseEntity.badRequest().body(FinalUtil.JWT_PROVIDED_IS_NOT_VALID);

        //se busca al usuario por su id y te devuelve el email y cuando expira este jwt
        // Search for the user by their id and return the email and when this jwt expires
        try {
            User user = userRepository.findById(new Long(claim.getSubject().trim())).get();
            return ResponseEntity.ok().body(FinalUtil.TE_HAS_LOGEADO_CON_EL_EMAIL + user.getEmail() + FinalUtil.TU_SESION_EXPIRA_EN + claim.getExpiration());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(FinalUtil.JWT_PROVIDED_IS_NOT_VALID);
        }
    }

    //funcion para enviar emails de verificacion
    // Function to send verification emails
    public boolean sendMail(User user) {
        boolean ok = false;
        try {
            // crea un mensaje de correo electrónico
            // creates an email message
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(user.getEmail().trim());
            helper.setSubject(FinalUtil.VERIFICA_TU_CUENTA);
            helper.setText(FinalUtil.VERIFY_CLICKING + user.getUuid());

            // envía el correo electrónico
            // sends the email
            javaMailSender.send(message);
            System.out.println("Mensaje enviado correctamente.");
            ok = true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return ok;
    }

    //funcion para activar al usuario
    //function to activate the user
    public ResponseEntity<String> activateUser(JsonUUID uuid, BindingResult bindingResult) {

        ResponseEntity<String> hasErrors = isACorrectStringData(uuid.getUuid(), bindingResult);
        if (hasErrors != null) return hasErrors;

        User user = userRepository.findByUuid(uuid.getUuid());

        if (user != null && user.getActive())
            return ResponseEntity.ok().body(FinalUtil.USER_WAS_ACTIVATED);

        else if (user != null && !user.getActive()) {
            user.setActive(true);
            userRepository.save(user);

            return getLogUser(user);
        }
        return ResponseEntity.badRequest().body(FinalUtil.UUID_DOESNT_MATCH);

    }

    //funcion para logear y devolver el jwt
    // Function for logging in and returning the JWT
    private ResponseEntity<String> getLogUser(User user) {
        User realUser = authenticate(user.getEmail(), user.getPassword());
        return realUser != null ? ResponseEntity.created(URI.create(FinalUtil.PATH_LOG + realUser.getId())).body(jwtUtils.generateJwt(realUser, true))
                : ResponseEntity.badRequest().body(FinalUtil.USER_DATA_DOES_NOT_MATCH);
    }

    //funcion para comprobar que los datos del usuario enviado son validos
    // Function to check if the user data sent is valid
    private static ResponseEntity<String> isACorrectUserData(User user, BindingResult bindingResult) {

        if (bindingResult.hasErrors() || user.getEmail().trim().isEmpty() || user.getPassword().trim().isEmpty())
            return ResponseEntity.badRequest().body(FinalUtil.DATA_PROVIDED_HAS_ERRORS);

        return null;
    }

    //funcion para comprobar que los datos del jwt enviado son correctos
    // Function to verify that the sent JWT data is correct
    private static ResponseEntity<String> isACorrectStringData(String string, BindingResult bindingResult) {

        if (bindingResult.hasErrors() || string.trim().isEmpty())
            return ResponseEntity.badRequest().body(FinalUtil.DATA_PROVIDED_HAS_ERRORS);

        return null;
    }
}
