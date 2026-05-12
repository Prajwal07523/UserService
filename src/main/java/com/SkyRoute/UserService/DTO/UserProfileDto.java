package com.SkyRoute.UserService.DTO;

import com.SkyRoute.UserService.Entity.Role;

public class UserProfileDto {
    private Long id;
    private String name;
    private String email;
    private Role role;

    public UserProfileDto(Long id, String name, String email, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
}
