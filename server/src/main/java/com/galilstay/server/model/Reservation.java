package com.galilstay.server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Guest guest;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Unit unit;

    @Column(nullable = false)
    private LocalDate arrival;

    @Column(nullable = false)
    private LocalDate departure;

    private int adults;
    private int children;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "reservation_extras")
    private Set<Extra> extras = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    private double amount;
    private double refund;

    @Column(length = 500)
    private String requests;

    private LocalDateTime createdAt;
}
