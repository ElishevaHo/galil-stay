package com.galilstay.server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** תוספת להזמנה - ארוחת בוקר, עיסוי, מארז יין וכו' */
@Entity
@Table(name = "extras")
@Getter
@Setter
@NoArgsConstructor
public class Extra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private double price;

    /** true - המחיר נגבה על כל לילה, false - תשלום חד-פעמי */
    private boolean chargedPerNight;
}
