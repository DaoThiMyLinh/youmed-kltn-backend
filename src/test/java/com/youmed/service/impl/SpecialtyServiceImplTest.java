package com.youmed.service.impl;

import com.youmed.dto.request.SpecialtyRequest;
import com.youmed.dto.response.SpecialtyResponse;
import com.youmed.entity.Specialty;
import com.youmed.exception.DuplicateResourceException;
import com.youmed.repository.SpecialtyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SpecialtyServiceImplTest {

    @Mock
    private SpecialtyRepository specialtyRepository;

    @InjectMocks
    private SpecialtyServiceImpl specialtyService;

    private SpecialtyRequest request;

    @BeforeEach
    void setUp() {
        request = new SpecialtyRequest();
        request.setName("tim mạch");
        request.setDescription("Mo ta tim mach");
        request.setImageUrl("url");
        request.setActive(true);
    }

    @Test
    void createSpecialty_ShouldNormalizeToTitleCase() {
        // Arrange
        when(specialtyRepository.existsByNameIgnoreCase("Tim Mạch")).thenReturn(false);
        
        Specialty savedSpecialty = Specialty.builder()
                .id(1L)
                .name("Tim Mạch")
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .active(request.getActive())
                .build();
                
        when(specialtyRepository.save(any(Specialty.class))).thenReturn(savedSpecialty);

        // Act
        SpecialtyResponse response = specialtyService.createSpecialty(request);

        // Assert
        assertEquals("Tim Mạch", response.getName());
        verify(specialtyRepository, times(1)).save(argThat(s -> s.getName().equals("Tim Mạch")));
    }

    @Test
    void createSpecialty_ShouldThrowException_WhenDuplicateNameExists_LowerCase() {
        // Arrange
        request.setName("tim mạch");
        when(specialtyRepository.existsByNameIgnoreCase("Tim Mạch")).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
            specialtyService.createSpecialty(request);
        });
        assertEquals("Specialty name already exists", exception.getMessage());
        verify(specialtyRepository, never()).save(any(Specialty.class));
    }

    @Test
    void createSpecialty_ShouldThrowException_WhenDuplicateNameExists_UpperCaseWithSpaces() {
        // Arrange
        request.setName("   TIM MẠCH   ");
        // "   TIM MẠCH   " normalizes to "Tim Mạch"
        when(specialtyRepository.existsByNameIgnoreCase("Tim Mạch")).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
            specialtyService.createSpecialty(request);
        });
        assertEquals("Specialty name already exists", exception.getMessage());
        verify(specialtyRepository, never()).save(any(Specialty.class));
    }
    
    @Test
    void createSpecialty_ShouldThrowException_WhenDuplicateNameExists_TitleCase() {
        // Arrange
        request.setName("Tim Mạch");
        when(specialtyRepository.existsByNameIgnoreCase("Tim Mạch")).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
            specialtyService.createSpecialty(request);
        });
        assertEquals("Specialty name already exists", exception.getMessage());
        verify(specialtyRepository, never()).save(any(Specialty.class));
    }
}
