package edu.cit.resuera.eventhive.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String status;
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
}