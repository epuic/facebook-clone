package facebook.server.crud;

import facebook.server.entity.User;
import facebook.server.repository.UserRepository;
import facebook.server.utilities.UserBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSaveUser() {
        User user = new UserBuilder()
                .withUsername("John Doe")
                .withEmail("john.doe@example.com")
                .withPassword("securepassword")
                .withUrlPhoto("http://example.com/photo.jpg")
                .withRole("USER")
                .build();

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo(user.getUsername());
        assertThat(savedUser.getEmail()).isEqualTo(user.getEmail());
        assertThat(savedUser.getPassword()).isEqualTo(user.getPassword());
        assertThat(savedUser.getUrlPhoto()).isEqualTo(user.getUrlPhoto());
        assertThat(savedUser.getRole()).isEqualTo(user.getRole());
        assertThat(savedUser.getCreatedAt()).isEqualTo(user.getCreatedAt());

        userRepository.deleteById(savedUser.getId());
    }

    @Test
    public void testFindUserById() {
        User user = new UserBuilder()
                .withUsername("Jane Doe")
                .withEmail("jane.doe@example.com")
                .build();

        User savedUser = userRepository.save(user);
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo(user.getUsername());
        assertThat(foundUser.get().getEmail()).isEqualTo(user.getEmail());

        userRepository.deleteById(savedUser.getId());
    }

    @Test
    public void testFindAllUsers() {
        User user1 = new UserBuilder().withUsername("User One").withEmail("user.one@example.com").build();
        User user2 = new UserBuilder().withUsername("User Two").withEmail("user.two@example.com").build();

        userRepository.save(user1);
        userRepository.save(user2);

        Iterable<User> users = userRepository.findAll();

        assertThat(users).hasSizeGreaterThanOrEqualTo(2);  // Ensures at least 2 users exist
        userRepository.deleteById(user1.getId());
        userRepository.deleteById(user2.getId());

    }

    @Test
    public void testDeleteUserById() {
        User user = new UserBuilder().withUsername("User To Delete").withEmail("delete.me@example.com").build();

        User savedUser = userRepository.save(user);
        userRepository.deleteById(savedUser.getId());

        Optional<User> deletedUser = userRepository.findById(savedUser.getId());

        assertThat(deletedUser).isNotPresent();
    }
}
