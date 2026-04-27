package com.mma.gestion.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final AuthEntryPointJwt unauthorizedHandler;

    private final UserDetailsService userDetailsService;
    
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
          .cors(Customizer.withDefaults())
          .authorizeHttpRequests(req -> req.requestMatchers("/auth/**") // Agregar las demás rutas públicas aquí
            .permitAll()
            .anyRequest()
            .authenticated())
          .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
          .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
          .authenticationProvider(authenticationProvider())
          .addFilterBefore(
              authenticationJwtTokenFilter(), 
              UsernamePasswordAuthenticationFilter.class
           );

        return http.build();
    }

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        
        authProvider.setUserDetailsService(userDetailsService); // Aquí se usa el service
        authProvider.setPasswordEncoder(passwordEncoder());    // Aquí se usa el encoder
        
        return authProvider;
    }
}
