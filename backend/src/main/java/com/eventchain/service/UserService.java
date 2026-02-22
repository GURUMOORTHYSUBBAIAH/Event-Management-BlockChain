package com.eventchain.service;

import com.eventchain.entity.User;
import com.eventchain.repository.UserRepository;
import com.eventchain.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        var principal = auth.getPrincipal();
        if (principal instanceof UserPrincipal up) {
            return userRepository.findById(up.getId()).orElse(null);
        }
        return null;
    }
}
