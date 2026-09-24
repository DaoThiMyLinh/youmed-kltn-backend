package com.youmed.controller;

import com.youmed.dto.request.RejectScheduleRequest;
import com.youmed.dto.response.DoctorResponse;
import com.youmed.dto.response.DoctorScheduleResponse;
import com.youmed.service.DepartmentManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/department-manager")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DEPARTMENT_MANAGER')")
public class DepartmentManagerController {

    private final DepartmentManagerService departmentManagerService;

    @GetMapping("/doctors")
    public List<DoctorResponse> getManagedDoctors() {
        return departmentManagerService.getManagedDoctors();
    }

    @GetMapping("/schedules")
    public List<DoctorScheduleResponse> getManagedSchedules() {
        return departmentManagerService.getManagedSchedules();
    }

    @PutMapping("/schedules/{scheduleId}/approve")
    public DoctorScheduleResponse approveSchedule(@PathVariable Long scheduleId) {
        return departmentManagerService.approveSchedule(scheduleId);
    }

    @PutMapping("/schedules/{scheduleId}/reject")
    public DoctorScheduleResponse rejectSchedule(
            @PathVariable Long scheduleId,
            @Valid @RequestBody RejectScheduleRequest request) {
        return departmentManagerService.rejectSchedule(scheduleId, request);
    }
}
