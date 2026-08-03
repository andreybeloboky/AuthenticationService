package com.beloboki.model;

import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class User {
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
    private Boolean active;
}
