package com.eventchain.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eventchain.dto.AuthResponse;
import com.eventchain.dto.LoginRequest;
import com.eventchain.dto.RefreshTokenRequest;
import com.eventchain.dto.RegisterRequest;
import com.eventchain.dto.WalletConnectRequest;
import com.eventchain.entity.RefreshToken;
import com.eventchain.entity.Role;
import com.eventchain.entity.User;
import com.eventchain.exception.BadRequestException;
import com.eventchain.repository.RefreshTokenRepository;
import com.eventchain.repository.RoleRepository;
import com.eventchain.repository.UserRepository;
import com.eventchain.security.JwtProvider;
import com.eventchain.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(Optional.ofNullable(request.getDisplayName())
                .orElseGet(() -> request.getEmail().split("@")[0]));
        user.setEnabled(true);
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("USER");
                    return roleRepository.save(r);
                });
        user.setRoles(Set.of(userRole));
        user = userRepository.save(user);

        var principal = UserPrincipal.create(user);
        String accessToken = jwtProvider.generateAccessToken(principal);
        String refreshToken = createRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        var principal = (UserPrincipal) auth.getPrincipal();
        String accessToken = jwtProvider.generateAccessToken(principal);
        String refreshToken = createRefreshToken(user);
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    public String generateAccessToken(UserPrincipal principal) {
        return jwtProvider.generateAccessToken(principal);
    }

    public String generateRefreshToken(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return createRefreshToken(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken token = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));
        if (token.getRevoked() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Refresh token expired or revoked");
        }
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        User user = token.getUser();
        var principal = UserPrincipal.create(user);
        String accessToken = jwtProvider.generateAccessToken(principal);
        String refreshToken = createRefreshToken(user);
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse walletConnect(WalletConnectRequest request) {
        User user = userRepository.findByWalletAddress(request.getWalletAddress())
                .orElseThrow(() -> new BadRequestException("Wallet not linked to account"));
        var principal = UserPrincipal.create(user);
        String accessToken = jwtProvider.generateAccessToken(principal);
        String refreshToken = createRefreshToken(user);
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    private String createRefreshToken(User user) {
        String token = jwtProvider.generateRefreshToken(user.getId());
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(604800));
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        AuthResponse.UserDto dto = new AuthResponse.UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setDisplayName(user.getDisplayName());
        dto.setWalletAddress(user.getWalletAddress());
        dto.setProfileImageUrl(user.getProfileImageUrl());

        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(accessTokenExpirationMs);
        response.setUser(dto);
        response.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        return response;
    }
}
