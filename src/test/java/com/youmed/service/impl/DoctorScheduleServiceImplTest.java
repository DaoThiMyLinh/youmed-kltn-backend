package com.youmed.service.impl;

import com.youmed.dto.request.DoctorScheduleRangeRequest;
import com.youmed.dto.request.DoctorScheduleRequest;
import com.youmed.entity.Doctor;
import com.youmed.entity.DoctorSchedule;
import com.youmed.entity.User;
import com.youmed.enums.Role;
import com.youmed.enums.ScheduleStatus;
import com.youmed.repository.DoctorRepository;
import com.youmed.repository.DoctorScheduleRepository;
import com.youmed.repository.UserRepository;
import com.youmed.service.TimeSlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorScheduleServiceImplTest {

    @Mock
    private DoctorScheduleRepository doctorScheduleRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TimeSlotService timeSlotService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DoctorScheduleServiceImpl doctorScheduleService;

    private User userA;
    private User userB;
    private Doctor doctorA;
    private Doctor doctorB;
    private DoctorSchedule scheduleA;
    private DoctorSchedule scheduleB;

    @BeforeEach
    void setUp() {
        userA = User.builder().id(1L).email("doctorA@test.com").role(Role.DOCTOR).build();
        userB = User.builder().id(2L).email("doctorB@test.com").role(Role.DOCTOR).build();

        doctorA = Doctor.builder().id(1L).user(userA).build();
        doctorB = Doctor.builder().id(2L).user(userB).build();

        scheduleA = DoctorSchedule.builder().id(1L).doctor(doctorA).status(ScheduleStatus.PENDING).build();
        scheduleB = DoctorSchedule.builder().id(2L).doctor(doctorB).status(ScheduleStatus.PENDING).build();
    }

    private void mockSecurityContext(String email) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void doctorA_deleteScheduleA_Success() {
        mockSecurityContext("doctorA@test.com");
        when(userRepository.findByEmail("doctorA@test.com")).thenReturn(Optional.of(userA));
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.of(doctorA));
        when(doctorScheduleRepository.findById(1L)).thenReturn(Optional.of(scheduleA));
        doNothing().when(timeSlotService).deleteSlotsBySchedule(1L);

        assertDoesNotThrow(() -> doctorScheduleService.deleteSchedule(1L));

        verify(doctorScheduleRepository, times(1)).delete(scheduleA);
    }

    @Test
    void doctorA_deleteScheduleB_Fails_CrossDoctor() {
        mockSecurityContext("doctorA@test.com");
        when(userRepository.findByEmail("doctorA@test.com")).thenReturn(Optional.of(userA));
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.of(doctorA));
        when(doctorScheduleRepository.findById(2L)).thenReturn(Optional.of(scheduleB));

        assertThrows(AccessDeniedException.class, () -> doctorScheduleService.deleteSchedule(2L));
        verify(doctorScheduleRepository, never()).delete(any());
    }

    @Test
    void doctorB_deleteScheduleA_Fails_CrossDoctor() {
        mockSecurityContext("doctorB@test.com");
        when(userRepository.findByEmail("doctorB@test.com")).thenReturn(Optional.of(userB));
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.of(doctorB));
        when(doctorScheduleRepository.findById(1L)).thenReturn(Optional.of(scheduleA));

        assertThrows(AccessDeniedException.class, () -> doctorScheduleService.deleteSchedule(1L));
        verify(doctorScheduleRepository, never()).delete(any());
    }

    @Test
    void doctor_createSchedule_ReturnsPending() {
        mockSecurityContext("doctorA@test.com");
        when(userRepository.findByEmail("doctorA@test.com")).thenReturn(Optional.of(userA));
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.of(doctorA));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctorA));
        when(doctorScheduleRepository.findByDoctorIdAndWorkingDate(eq(1L), any(LocalDate.class))).thenReturn(Optional.empty());

        when(doctorScheduleRepository.save(any(DoctorSchedule.class))).thenAnswer(invocation -> {
            DoctorSchedule schedule = invocation.getArgument(0);
            schedule.setId(10L);
            return schedule;
        });

        DoctorScheduleRequest request = new DoctorScheduleRequest();
        request.setDoctorId(1L);
        request.setWorkingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(8, 0));
        request.setEndTime(LocalTime.of(12, 0));

        var response = doctorScheduleService.createSchedule(request);

        assertEquals(ScheduleStatus.PENDING, response.getStatus());
        assertTrue(response.getActive());
        assertNull(response.getRejectionReason());
    }

    @Test
    void doctor_createScheduleRange_ReturnsPending() {
        mockSecurityContext("doctorA@test.com");
        when(userRepository.findByEmail("doctorA@test.com")).thenReturn(Optional.of(userA));
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.of(doctorA));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctorA));
        when(doctorScheduleRepository.findByDoctorIdAndWorkingDate(eq(1L), any(LocalDate.class))).thenReturn(Optional.empty());

        when(doctorScheduleRepository.save(any(DoctorSchedule.class))).thenAnswer(invocation -> {
            DoctorSchedule schedule = invocation.getArgument(0);
            assertEquals(ScheduleStatus.PENDING, schedule.getStatus());
            assertTrue(schedule.getActive());
            assertNull(schedule.getRejectionReason());
            schedule.setId(10L);
            return schedule;
        });

        DoctorScheduleRangeRequest request = new DoctorScheduleRangeRequest();
        request.setDoctorId(1L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(2));
        request.setStartTime(LocalTime.of(8, 0));
        request.setEndTime(LocalTime.of(12, 0));

        var response = doctorScheduleService.createScheduleRange(request);

        assertEquals(2, response.getCreatedDates().size());
        verify(doctorScheduleRepository, times(2)).save(any(DoctorSchedule.class));
    }
}
