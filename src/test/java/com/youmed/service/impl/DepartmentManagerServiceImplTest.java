package com.youmed.service.impl;

import com.youmed.dto.request.RejectScheduleRequest;
import com.youmed.entity.Doctor;
import com.youmed.entity.DoctorSchedule;
import com.youmed.entity.Specialty;
import com.youmed.entity.User;
import com.youmed.enums.Role;
import com.youmed.enums.ScheduleStatus;
import com.youmed.exception.BadRequestException;
import com.youmed.repository.DoctorRepository;
import com.youmed.repository.DoctorScheduleRepository;
import com.youmed.repository.SpecialtyRepository;
import com.youmed.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentManagerServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SpecialtyRepository specialtyRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorScheduleRepository doctorScheduleRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DepartmentManagerServiceImpl departmentManagerService;

    private User managerA;
    private User managerB;
    private Specialty specialtyA;
    private Specialty specialtyB;
    private Doctor doctorA;
    private Doctor doctorB;
    private DoctorSchedule scheduleA;
    private DoctorSchedule scheduleB;

    @BeforeEach
    void setUp() {
        managerA = User.builder().id(1L).email("managerA@test.com").role(Role.DEPARTMENT_MANAGER).build();
        managerB = User.builder().id(2L).email("managerB@test.com").role(Role.DEPARTMENT_MANAGER).build();

        specialtyA = Specialty.builder().id(1L).manager(managerA).build();
        specialtyB = Specialty.builder().id(2L).manager(managerB).build();

        doctorA = Doctor.builder().id(1L).specialty(specialtyA).user(User.builder().fullName("Doctor A").build()).build();
        doctorB = Doctor.builder().id(2L).specialty(specialtyB).user(User.builder().fullName("Doctor B").build()).build();

        scheduleA = DoctorSchedule.builder().id(1L).doctor(doctorA).status(ScheduleStatus.PENDING).build();
        scheduleB = DoctorSchedule.builder().id(2L).doctor(doctorB).status(ScheduleStatus.PENDING).build();
    }

    private void mockSecurityContext(String email) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void managerA_approveScheduleA_Success() {
        mockSecurityContext("managerA@test.com");
        when(userRepository.findByEmail("managerA@test.com")).thenReturn(Optional.of(managerA));
        when(specialtyRepository.findByManagerId(1L)).thenReturn(Optional.of(specialtyA));
        when(doctorScheduleRepository.findById(1L)).thenReturn(Optional.of(scheduleA));
        when(doctorScheduleRepository.save(any(DoctorSchedule.class))).thenReturn(scheduleA);

        var response = departmentManagerService.approveSchedule(1L);

        assertEquals(ScheduleStatus.APPROVED, scheduleA.getStatus());
        assertEquals(ScheduleStatus.APPROVED, response.getStatus());
    }

    @Test
    void managerA_approveScheduleB_Fails_CrossDepartment() {
        mockSecurityContext("managerA@test.com");
        when(userRepository.findByEmail("managerA@test.com")).thenReturn(Optional.of(managerA));
        when(specialtyRepository.findByManagerId(1L)).thenReturn(Optional.of(specialtyA));
        when(doctorScheduleRepository.findById(2L)).thenReturn(Optional.of(scheduleB));

        assertThrows(AccessDeniedException.class, () -> departmentManagerService.approveSchedule(2L));
    }

    @Test
    void managerB_approveScheduleA_Fails_CrossDepartment() {
        mockSecurityContext("managerB@test.com");
        when(userRepository.findByEmail("managerB@test.com")).thenReturn(Optional.of(managerB));
        when(specialtyRepository.findByManagerId(2L)).thenReturn(Optional.of(specialtyB));
        when(doctorScheduleRepository.findById(1L)).thenReturn(Optional.of(scheduleA));

        assertThrows(AccessDeniedException.class, () -> departmentManagerService.approveSchedule(1L));
    }

    @Test
    void managerA_rejectScheduleA_Success() {
        mockSecurityContext("managerA@test.com");
        when(userRepository.findByEmail("managerA@test.com")).thenReturn(Optional.of(managerA));
        when(specialtyRepository.findByManagerId(1L)).thenReturn(Optional.of(specialtyA));
        when(doctorScheduleRepository.findById(1L)).thenReturn(Optional.of(scheduleA));
        when(doctorScheduleRepository.save(any(DoctorSchedule.class))).thenReturn(scheduleA);

        RejectScheduleRequest request = new RejectScheduleRequest();
        request.setReason("Busy");

        var response = departmentManagerService.rejectSchedule(1L, request);

        assertEquals(ScheduleStatus.REJECTED, scheduleA.getStatus());
        assertEquals("Busy", scheduleA.getRejectionReason());
        assertEquals(ScheduleStatus.REJECTED, response.getStatus());
    }

    @Test
    void managerA_rejectScheduleB_Fails_CrossDepartment() {
        mockSecurityContext("managerA@test.com");
        when(userRepository.findByEmail("managerA@test.com")).thenReturn(Optional.of(managerA));
        when(specialtyRepository.findByManagerId(1L)).thenReturn(Optional.of(specialtyA));
        when(doctorScheduleRepository.findById(2L)).thenReturn(Optional.of(scheduleB));

        RejectScheduleRequest request = new RejectScheduleRequest();
        request.setReason("Busy");

        assertThrows(AccessDeniedException.class, () -> departmentManagerService.rejectSchedule(2L, request));
    }

    @Test
    void managerA_approveApprovedSchedule_Fails() {
        mockSecurityContext("managerA@test.com");
        when(userRepository.findByEmail("managerA@test.com")).thenReturn(Optional.of(managerA));
        when(specialtyRepository.findByManagerId(1L)).thenReturn(Optional.of(specialtyA));
        scheduleA.setStatus(ScheduleStatus.APPROVED);
        when(doctorScheduleRepository.findById(1L)).thenReturn(Optional.of(scheduleA));

        assertThrows(BadRequestException.class, () -> departmentManagerService.approveSchedule(1L));
    }
}
