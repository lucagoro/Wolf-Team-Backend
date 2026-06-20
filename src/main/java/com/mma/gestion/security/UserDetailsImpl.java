package com.mma.gestion.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mma.gestion.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    @JsonIgnore // Por seguridad, para que no se mande la pass en ningún JSON accidentally
    private String password;
    private Collection<? extends GrantedAuthority> authorities;


    public static UserDetailsImpl build(User user) {
    // Creamos una autoridad única a partir del nombre del Enum (ej: "ROLE_ADMIN")
    GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().name());

    return new UserDetailsImpl(
            user.getId(),
            user.getUsername(),
            user.getPassword(),
            Collections.singletonList(authority) // Envolvemos la autoridad en una lista de un solo elemento
    );
}

    // Métodos obligatorios de la interfaz. Normalmente devolvemos true en todos.
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}  
