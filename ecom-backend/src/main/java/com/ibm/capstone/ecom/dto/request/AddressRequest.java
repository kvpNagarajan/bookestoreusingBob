package com.ibm.capstone.ecom.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressRequest {

    @NotBlank @Size(max = 100)
    private String fullName;

    @NotBlank
    private String addressLine1;

    private String addressLine2;

    @NotBlank @Size(max = 100)
    private String city;

    @NotBlank @Size(max = 100)
    private String state;

    @NotBlank @Size(max = 20)
    private String postalCode;

    @NotBlank @Size(max = 60)
    private String country;

    @Size(max = 20)
    private String phone;

    private Boolean isDefault;
}
