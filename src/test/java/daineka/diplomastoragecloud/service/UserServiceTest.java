package daineka.diplomastoragecloud.service;

import daineka.diplomastoragecloud.DiplomaStorageCloudApplication;
import daineka.diplomastoragecloud.dto.AuthentificationRequest;
import daineka.diplomastoragecloud.dto.AuthentificationResponse;
import daineka.diplomastoragecloud.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Objects;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = DiplomaStorageCloudApplication.class)
@AutoConfigureMockMvc
public class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthentificationService authentificationService;

    @Test
    public void findUserByLoginAndPasswordTest() {
        var user = userRepository.findUserByLoginAndPassword("Виталий", "qwerty");
        assertTrue(user.isPresent());
        assertEquals("Виталий", user.get().getLogin());
    }

    @Test
    public void authentificationLoginTest() {
        AuthentificationResponse response = authentificationService.authentificationLogin(new AuthentificationRequest("Степан", "qwe123"));
        assertNotNull(response);
        assertNotNull(Objects.requireNonNull(response).getAuthToken());
        assertNotEquals(0, response.getAuthToken().length());
    }

    @Test
    public void authentificationLoginExceptionTest() {
        AuthentificationResponse actual = authentificationService.authentificationLogin(new AuthentificationRequest("Гриша", "11111111"));
        assertNull(actual);
    }

    @Test
    public void logoutTest() {
        AuthentificationResponse response = authentificationService.authentificationLogin(new AuthentificationRequest("Алла", "123ewq"));
        String authToken = Objects.requireNonNull(response).getAuthToken();

        boolean actual = authentificationService.logout(authToken);
        boolean expected = true;
       assertEquals(expected, actual);
    }
}
