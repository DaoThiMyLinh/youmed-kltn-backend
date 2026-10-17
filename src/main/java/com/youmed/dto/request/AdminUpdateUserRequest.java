package com.youmed.dto.request;

import com.youmed.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUpdateUserRequest {
    
    @NotNull(message = "Role is required")
    private Role role;
    
    @NotNull(message = "Enabled status is required")
    private Boolean enabled;
}
