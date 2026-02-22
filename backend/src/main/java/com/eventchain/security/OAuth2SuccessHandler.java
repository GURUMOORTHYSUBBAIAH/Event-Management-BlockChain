package com.eventchain.security;

import com.eventchain.entity.Role;
import com.eventchain.entity.User;
import com.eventchain.repository.RoleRepository;
import com.eventchain.repository.UserRepository;
import com.eventchain.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthService authService;

    public OAuth2SuccessHandler(UserRepository userRepository, RoleRepository roleRepository,
                                @Lazy AuthService authService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.authService = authService;
    }

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauthUser = oauthToken.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String picture = oauthUser.getAttribute("picture");

        User user = userRepository.findByOauthProviderAndOauthId("GOOGLE", oauthUser.getName())
                .or(() -> userRepository.findByEmail(email))
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setDisplayName(name);
                    newUser.setProfileImageUrl(picture);
                    newUser.setOauthProvider("GOOGLE");
                    newUser.setOauthId(oauthUser.getName());
                    newUser.setEnabled(true);
                    Role userRole = roleRepository.findByName("USER").orElseThrow();
                    newUser.setRoles(Set.of(userRole));
                    return userRepository.save(newUser);
                });

        var principal = UserPrincipal.create(user);
        String accessToken = authService.generateAccessToken(principal);
        String refreshToken = authService.generateRefreshToken(user.getId());

        String redirectUrl = String.format("%s/oauth2/callback?access_token=%s&refresh_token=%s",
                frontendUrl, accessToken, refreshToken);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
