package com.will.compairator.user.services;

import com.will.compairator.user.dto.UserDTO;
import com.will.compairator.user.model.UserEntity;
import com.will.compairator.user.persistence.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.Locale;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public UserDTO.Profile register(UserDTO.Registration input) {
        UserEntity user = new UserEntity();
        user.setEmail(normalize(input.email()));
        user.setPasswordHash(encoder.encode(input.password()));
        user.setCreatedAt(Instant.now());
        try {
            return UserDTO.Profile.from(users.saveAndFlush(user));
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette adresse est déjà utilisée.");
        }
    }

    public UserEntity find(String email) {
        return users.findByEmail(normalize(email))
                .orElseThrow(() -> new UsernameNotFoundException("Compte introuvable"));
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        UserEntity user = find(email);
        return User.withUsername(user.getEmail()).password(user.getPasswordHash())
                .roles(user.getRole()).build();
    }

    public void recordLogin(String email) {
        UserEntity user = find(email);
        user.setLastLoginAt(Instant.now());
        users.save(user);
    }

    private static String normalize(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }
}
