package com.greenkitchen.portal.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "otp_records")
public class OtpRecords extends AbstractEntity {

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "OTP code is required")
    private String otpCode;

    private LocalDateTime expiredAt;

    private Boolean isUsed = false;

    // Helper method to check if OTP is expired
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiredAt);
    }

    // Helper method to check if OTP is valid (not used and not expired)
    public boolean isValid() {
        return !this.isUsed && !this.isExpired();
    }
}
