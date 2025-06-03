package facebook.auth.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import facebook.auth.utilities.UserAuthentificationBuilder;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("username")
    private String username;

    @JsonProperty("email")
    private String email;

    @JsonProperty("password")
    private String password;

    @JsonProperty("profile_picture")
    private String urlPhoto;

    @JsonProperty("role")
    private String role;

    @JsonProperty("created_at")
    private String createdAt;

    public User(Long id, String username, String email, String password, String role, String createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.urlPhoto = "url";
        this.role = role;
        this.createdAt = createdAt;
    }
}
