package com.campusseekers.dto;

import com.campusseekers.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserResponse {
    private UUID id;
    private String email;
    private Role role;
    private Instant createdAt;
}
