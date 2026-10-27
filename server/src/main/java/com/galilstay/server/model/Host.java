package com.galilstay.server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** משתמש מנהל (בעל המתחם). הסיסמה נשמרת מוצפנת ב-BCrypt */
@Entity
@Table(name = "hosts")
@Getter
@Setter
@NoArgsConstructor
public class Host {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    private String displayName;
}
