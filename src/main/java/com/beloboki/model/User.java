package com.beloboki.model;

import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class User {
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
    private Boolean active;
}
