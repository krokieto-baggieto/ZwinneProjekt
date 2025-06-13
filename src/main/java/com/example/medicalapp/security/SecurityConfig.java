package com.example.medicalapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/home", "/register", "/login", "/login/patient", "/login/doctor", 
                                "/css/**", "/images/**", "/h2-console/**").permitAll()
                .requestMatchers("/doctor/**").hasRole("DOCTOR")
                .requestMatchers("/appointments/**").hasAnyRole("PATIENT", "DOCTOR")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .failureHandler((request, response, exception) -> {
                    String referrer = request.getHeader("Referer");
                    if (referrer != null) {
                        if (referrer.contains("/login/patient")) {
                            response.sendRedirect("/login/patient?error=true");
                        } else if (referrer.contains("/login/doctor")) {
                            response.sendRedirect("/login/doctor?error=true");
                        } else {
                            response.sendRedirect("/login?error=true");
                        }
                    } else {
                        response.sendRedirect("/login?error=true");
                    }
                })
                .successHandler((request, response, authentication) -> {
                    // Przekierowanie w zależności od roli
                    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                    if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                        response.sendRedirect("/admin");
                    } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"))) {
                        response.sendRedirect("/doctor/appointments");
                    } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"))) {
                        response.sendRedirect("/appointments");
                    } else {
                        response.sendRedirect("/");
                    }
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/access-denied")
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendRedirect("/error");
                })
            )
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?expired")
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}