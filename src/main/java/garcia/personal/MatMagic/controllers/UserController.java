package garcia.personal.MatMagic.controllers;


import garcia.personal.MatMagic.models.JwtAgpRequest;
import garcia.personal.MatMagic.models.User;
import garcia.personal.MatMagic.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    @Autowired
    private UserService userService;


    @GetMapping
    public List<User> listAll() {
        return userService.listAll();
    }

    @PostMapping("/create")
    public ResponseEntity<String> createUser(@Valid @RequestBody User user, BindingResult bindingResult) {
        return userService.getUserResponseEntity(user, bindingResult);
    }

    @PostMapping("/log")
    public ResponseEntity<String> logUser(@Valid @RequestBody User user, BindingResult bindingResult) {
        return userService.getLogUser(user, bindingResult);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyJwt(@Valid @RequestBody JwtAgpRequest jwt, BindingResult bindingResult) {
        return userService.verifyJwt(jwt, bindingResult);
    }


}

