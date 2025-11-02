package com.example.UserService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name="users")
@Data
@NoArgsConstructor
public class User {
    @Id
    private String username;
    private String password;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name="roles",columnDefinition = "text[]")
    private String[] roles;
    public User(String username, String password,String[] roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }
}
