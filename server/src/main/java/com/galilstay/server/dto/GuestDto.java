package com.galilstay.server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GuestDto {
    private Long id;

    @NotBlank(message = "יש להזין שם פרטי")
    private String firstName;

    @NotBlank(message = "יש להזין שם משפחה")
    private String lastName;

    @NotBlank(message = "יש להזין אימייל")
    @Email(message = "כתובת אימייל לא תקינה")
    private String email;

    @Pattern(regexp = "^$|^0\\d{1,2}-?\\d{7}$", message = "מספר טלפון לא תקין")
    private String phone;

    private String city;
    private LocalDate joinedOn;

    // שדות מחושבים
    private int stays;
    private double totalSpent;
}
