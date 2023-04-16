package garcia.personal.MatMagic.unittest;
import garcia.personal.MatMagic.models.User;
import garcia.personal.MatMagic.repositories.UserRepository;
import garcia.personal.MatMagic.services.UserService;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

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

}
