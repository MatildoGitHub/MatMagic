package garcia.personal.MatMagic.unittest;

import garcia.personal.MatMagic.models.User;
import garcia.personal.MatMagic.repositories.UserRepository;
import garcia.personal.MatMagic.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class JwtUtilsTests {

    @InjectMocks
    private JwtUtils jwtUtils;
    @Mock
    private UserRepository userRepository;


    @Test
    public void testJwtWorksFine() {
        User user = new User("valid_email@example.com", "password");
        user.setId(1L);
        when(userRepository.findByEmail("valid_email@example.com")).thenReturn(user);
        User databaseUser = userRepository.findByEmail("valid_email@example.com");
        String jwt = jwtUtils.generateJwt(user,false);

        Claims claim = jwtUtils.validateJwtAndGetClaims(jwt,false);

        assertEquals(databaseUser.getId().toString(), claim.getSubject());
    }

}
