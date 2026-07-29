package com.beloboki.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class User {

    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
    private Boolean active;

}
