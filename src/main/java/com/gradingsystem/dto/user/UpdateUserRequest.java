package com.gradingsystem.dto.user;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/**
 * All fields are optional — only non-null fields are applied.
 * Validation annotations are null-safe (they are skipped when the field is null).
 */
@Getter
@Setter
public class UpdateUserRequest {

    @Size(min = 3, max = 50, message = "Username must be 3–50 characters")
    private String username;

    @Email(message = "Must be a valid email address")
    @Size(max = 150)
    private String email;

    /** When supplied, the new password must meet the same strength rules as creation. */
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp  = "^(?=.*[A-Za-z])(?=.*\\d).+$",
        message = "Password must contain at least one letter and one number"
    )
    private String password;

    @Size(max = 150, message = "Full name must not exceed 150 characters")
    private String fullName;
}
