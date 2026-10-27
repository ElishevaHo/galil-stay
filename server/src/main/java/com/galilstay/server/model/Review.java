package com.galilstay.server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** חוות דעת של אורח - ניתנת רק על הזמנה שהסתיימה */
@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(unique = true)
    private Reservation reservation;

    /** 1 עד 5 */
    private int stars;

    @Column(length = 1000)
    private String text;

    private LocalDate writtenOn;
}
