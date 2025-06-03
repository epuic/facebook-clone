package facebook.auth.service;

import facebook.auth.entity.UserAuthentification;
import facebook.auth.repository.UserAuthentificationRepository;
import facebook.auth.utilities.JWTUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
    @Autowired
    private UserAuthentificationRepository userRepository;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private JWTUtils jwtUtils;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow();
    }

    public UserAuthentification getUserFromJWT(){
        final String jwtToken = request.getHeader("Authorization")
                .substring(7);
        if (jwtToken.isEmpty()) return null;

        return userRepository.findByEmail(jwtUtils.extractUsername(jwtToken)).get();
    }

    public UserAuthentification getUser(Long id){
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }
}
