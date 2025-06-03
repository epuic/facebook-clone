package facebook.server.utilities;

import facebook.server.dto.UserDTO;
import facebook.server.entity.User;

public class DTOBuilder {
    private UserDTO userDTO;

    public DTOBuilder() {
        userDTO = new UserDTO();
        userDTO.setStatusCode(200); // default status code
        userDTO.setMessage("Success"); // default message
        userDTO.setUser(null);
        userDTO.setToken("");
        userDTO.setError("");
        userDTO.setExpirationTime("");
        userDTO.setExpirationTime("");
    }

    public DTOBuilder withStatusCode(int statusCode) {
        userDTO.setStatusCode(statusCode);
        return this;
    }

    public DTOBuilder withError(String error) {
        userDTO.setError(error);
        return this;
    }

    public DTOBuilder withMessage(String message) {
        userDTO.setMessage(message);
        return this;
    }

    public DTOBuilder withToken(String token) {
        userDTO.setToken(token);
        return this;
    }

    public DTOBuilder withRefreshToken(String refreshToken) {
        userDTO.setRefreshToken(refreshToken);
        return this;
    }

    public DTOBuilder withExpirationTime(String expirationTime) {
        userDTO.setExpirationTime(expirationTime);
        return this;
    }

    public DTOBuilder withUser(User user) {
        userDTO.setUser(user);
        return this;
    }

    public UserDTO build() {
        return userDTO;
    }
}