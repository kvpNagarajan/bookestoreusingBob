package com.ibm.capstone.ecom.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "addresses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "full_name", nullable = false, length = 100)
    @NotBlank
    private String fullName;

    @Column(name = "address_line1", nullable = false)
    @NotBlank
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(nullable = false, length = 100)
    @NotBlank
    private String city;

    @Column(nullable = false, length = 100)
    @NotBlank
    private String state;

    @Column(nullable = false, length = 20)
    @NotBlank
    private String postalCode;

    @Column(nullable = false, length = 60)
    @NotBlank
    private String country;

    @Column(length = 20)
    private String phone;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
