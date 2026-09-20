package com.ibm.capstone.ecom.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class AddressResponse {
    private Long id;
    private String fullName;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String phone;
    private Boolean isDefault;
}
