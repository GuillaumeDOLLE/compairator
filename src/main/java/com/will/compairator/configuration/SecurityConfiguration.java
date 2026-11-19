package com.will.compairator.configuration;

import com.will.compairator.user.services.UserService;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserService users) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                        .requestMatchers("/admin/**", "/compairator-ui/admin.html").hasRole("ADMIN")
                        .requestMatchers("/", "/web", "/web/", "/compairator-ui/**", "/auth/csrf", "/auth/register").permitAll()
                        .requestMatchers("/stats/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, ex) -> response.sendError(401))
                        .accessDeniedHandler((request, response, ex) -> response.sendError(403)))
                .formLogin(login -> login.loginProcessingUrl("/auth/login")
                        .successHandler((request, response, authentication) -> {
                            users.recordLogin(authentication.getName());
                            response.setStatus(204);
                        })
                        .failureHandler((request, response, ex) -> response.sendError(401))
                        .permitAll())
                .logout(logout -> logout.logoutUrl("/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> response.setStatus(204)))
                .requestCache(cache -> cache.disable())
                .build();
    }
}
