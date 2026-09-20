package com.ibm.capstone.ecom.dto.response;

import com.ibm.capstone.ecom.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private User.Role role;
    private Integer giftPoints;
    private LocalDateTime createdAt;
}
