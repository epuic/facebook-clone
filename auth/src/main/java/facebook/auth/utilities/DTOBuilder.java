package facebook.auth.utilities;

import facebook.auth.dto.UserDTO;
import facebook.auth.entity.User;

public class DTOBuilder {
    private final UserDTO userDTO;

    public DTOBuilder() {
        userDTO = new UserDTO();
        userDTO.setStatusCode(200);
        userDTO.setMessage("Default");
        userDTO.setExpirationTime("0Hrs");
        userDTO.setToken("Default");
        userDTO.setRefreshToken("Default");
        userDTO.setUser(null);
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