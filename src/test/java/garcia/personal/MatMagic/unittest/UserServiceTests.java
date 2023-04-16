package garcia.personal.MatMagic.unittest;
import garcia.personal.MatMagic.models.User;
import garcia.personal.MatMagic.repositories.UserRepository;
import garcia.personal.MatMagic.services.UserService;
import garcia.personal.MatMagic.utils.PasswordEncoder;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;


    @Test
    public void testAuthenticateReturnsNullWhenUserNotFound() {
        // Arrange
        String email = "invalid_email@example.com";
        String password = "password";
        when(userRepository.findByEmail(email)).thenReturn(null);

        // Act
        User result = userService.authenticate(email, password);

        // Assert
        assertNull(result);
    }

    @Test
    public void testAuthenticateReturnsUserWhenPasswordMatches() {
        // Arrange
        String email = "valid_email@example.com";
        String password = "1234";
        User user = new User(email, PasswordEncoder.encode(password));
        when(userRepository.findByEmail(email)).thenReturn(user);

        // Act
        User result = userService.authenticate(email, password);

        // Assert
        assertEquals(user, result);
    }

    @Test
    public void testAuthenticateReturnsNullWhenPasswordDoesNotMatch() {
        // Arrange
        String email = "valid_email@example.com";
        String password = "invalid_password";
        User user = new User(email, PasswordEncoder.encode("password"));
        when(userRepository.findByEmail(email)).thenReturn(user);

        // Act
        User result = userService.authenticate(email, password);

        // Assert
        assertNull(result);
    }

}
