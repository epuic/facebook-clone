package facebook.auth.dto;

import lombok.Data;

@Data
public class ResetDTO {
    private String email;
    private String newPassword;
    private String resetCode;
}
