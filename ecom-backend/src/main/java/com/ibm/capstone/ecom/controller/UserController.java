package com.ibm.capstone.ecom.controller;

import com.ibm.capstone.ecom.dto.request.AddressRequest;
import com.ibm.capstone.ecom.dto.response.AddressResponse;
import com.ibm.capstone.ecom.dto.response.ApiResponse;
import com.ibm.capstone.ecom.dto.response.UserResponse;
import com.ibm.capstone.ecom.entity.Address;
import com.ibm.capstone.ecom.entity.User;
import com.ibm.capstone.ecom.exception.BusinessException;
import com.ibm.capstone.ecom.exception.ResourceNotFoundException;
import com.ibm.capstone.ecom.repository.AddressRepository;
import com.ibm.capstone.ecom.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User profile and address management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public UserController(UserRepository userRepository,
                          AddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(
            @AuthenticationPrincipal UserDetails principal) {
        User user = resolve(principal);
        return ResponseEntity.ok(ApiResponse.ok(toUserResponse(user)));
    }

    @GetMapping("/me/addresses")
    @Operation(summary = "Get all saved addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = resolve(principal).getId();
        List<AddressResponse> addresses = addressRepository.findByUserId(userId)
                .stream().map(this::toAddressResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(addresses));
    }

    @PostMapping("/me/addresses")
    @Operation(summary = "Add a new delivery address")
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody AddressRequest request) {
        User user = resolve(principal);
        // If this is set as default, remove default from others
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .ifPresent(a -> { a.setIsDefault(false); addressRepository.save(a); });
        }
        Address address = Address.builder()
                .user(user)
                .fullName(request.getFullName())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .phone(request.getPhone())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Address saved", toAddressResponse(addressRepository.save(address))));
    }

    @DeleteMapping("/me/addresses/{id}")
    @Operation(summary = "Delete a saved address")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        Long userId = resolve(principal).getId();
        if (!addressRepository.existsByIdAndUserId(id, userId)) {
            throw new BusinessException("Address not found or does not belong to you");
        }
        addressRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Address deleted", null));
    }

    private User resolve(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .giftPoints(user.getGiftPoints())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private AddressResponse toAddressResponse(Address a) {
        return AddressResponse.builder()
                .id(a.getId())
                .fullName(a.getFullName())
                .addressLine1(a.getAddressLine1())
                .addressLine2(a.getAddressLine2())
                .city(a.getCity())
                .state(a.getState())
                .postalCode(a.getPostalCode())
                .country(a.getCountry())
                .phone(a.getPhone())
                .isDefault(a.getIsDefault())
                .build();
    }
}
