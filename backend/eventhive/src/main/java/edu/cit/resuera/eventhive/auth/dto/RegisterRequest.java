package edu.cit.resuera.eventhive.dto;

import lombok.Data;

@Data
public class RegisterRequest {

    private String firstname;
    private String lastname;
    private String email;
    private String password;

}