package com.mma.gestion.dto;

import com.mma.gestion.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationDTO {
    private String username;
    private String password;
    private Role role;

}
