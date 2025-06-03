package facebook.server.utilities;

import facebook.server.entity.User;

import java.time.LocalDateTime;

public class UserBuilder {
    private final User user;

    public UserBuilder() {
        user = new User();
        user.setName("Default User");
        user.setEmail("default@example.com");
        user.setPassword("securepassword");
        user.setUrlPhoto("-");
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now().toString());
    }

    public UserBuilder withUsername(String username) {
        user.setName(username);
        return this;
    }

    public UserBuilder withEmail(String email) {
        user.setEmail(email);
        return this;
    }

    public UserBuilder withPassword(String password) {
        user.setPassword(password);
        return this;
    }

    public UserBuilder withUrlPhoto(String urlPhoto) {
        user.setUrlPhoto(urlPhoto);
        return this;
    }

    public UserBuilder withRole(String role) {
        user.setRole(role);
        return this;
    }
    public User build() {
        return user;
    }

    public static User toUser(String payload) {
        String[] parts = payload.split(", ");
        User user = new User();
        user.setId(null); // fixing "row was updated or deleted by another transaction" error

        for (String part : parts) {
            String[] keyValue = part.split("=");
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            switch (key) {
                case "username":
                    user.setName(value);
                    break;
                case "email":
                    user.setEmail(value);
                    break;
                case "urlPhoto":
                    user.setUrlPhoto(value);
                    break;
                case "role":
                    user.setRole(value);
                    break;
                case "createdAt":
                    user.setCreatedAt(value);
                    break;
                case "password":
                    user.setPassword(value);
                    break;
            }
        }
        return user;
    }
}
