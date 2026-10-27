package com.galilstay.server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** יחידת נופש - צימר, וילה או סוויטה */
@Entity
@Table(name = "units")
@Getter
@Setter
@NoArgsConstructor
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnitType kind;

    private String region;

    /** מחיר ללילה באמצע שבוע */
    private double weekdayRate;

    /** מחיר ללילה בסוף שבוע (חמישי ושישי בלילה) */
    private double weekendRate;

    /** דמי ניקיון חד-פעמיים */
    private double cleaningFee;

    private int capacity;
    private int bedrooms;
    private int minNights;

    private boolean privatePool;
    private boolean jacuzzi;
    private boolean petsAllowed;

    @Column(length = 1000)
    private String description;

    /** האם היחידה פתוחה להזמנות */
    private boolean listed = true;
}
