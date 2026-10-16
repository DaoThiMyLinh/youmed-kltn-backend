package com.youmed.service.impl;

import com.youmed.dto.request.DoctorCreateRequest;
import com.youmed.dto.request.DoctorUpdateRequest;
import com.youmed.dto.response.DoctorResponse;
import com.youmed.entity.Doctor;
import com.youmed.repository.DoctorRepository;
import com.youmed.repository.SpecialtyRepository;
import com.youmed.entity.Specialty;
import com.youmed.dto.response.SpecialtyResponse;
import com.youmed.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.youmed.repository.UserRepository;
import com.youmed.entity.User;
import com.youmed.enums.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.youmed.repository.AppointmentRepository;
import com.youmed.repository.DoctorScheduleRepository;
import com.youmed.repository.TimeSlotRepository;
import com.youmed.entity.DoctorSchedule;
import com.youmed.entity.TimeSlot;
import com.youmed.exception.BadRequestException;
import com.youmed.exception.ResourceNotFoundException;
import com.youmed.exception.DuplicateResourceException;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final SpecialtyRepository specialtyRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.user.default-password:123456}")
    private String defaultPassword;

    @Override
    public DoctorResponse createDoctor(DoctorCreateRequest request) {

        Role targetRole = (request.getRole() == Role.DEPARTMENT_MANAGER) ? Role.DEPARTMENT_MANAGER : Role.DOCTOR;

        Specialty specialty = null;
        if (request.getSpecialtyId() != null) {
            specialty = specialtyRepository.findById(request.getSpecialtyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Specialty not found"));
        }

        if (targetRole == Role.DEPARTMENT_MANAGER) {
            if (specialty == null) {
                throw new BadRequestException("Trưởng khoa bắt buộc phải được gắn với một chuyên khoa");
            }
            if (specialty.getManager() != null) {
                User existingUser = userRepository.findByEmail(request.getEmail()).orElse(null);
                if (existingUser == null || !specialty.getManager().getId().equals(existingUser.getId())) {
                    throw new DuplicateResourceException("Chuyên khoa " + specialty.getName() + " đã có Trưởng khoa (" + specialty.getManager().getFullName() + "). Mỗi chuyên khoa chỉ được có 1 Trưởng khoa.");
                }
            }
        }

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user != null) {
            if (user.getRole() == targetRole) {
                throw new DuplicateResourceException("Email đã tồn tại với vai trò này");
            }
            user.setRole(targetRole);
            user.setFullName(request.getFullName());
            user.setPhone(request.getPhone());
        } else {
            if (request.getPhone() != null && !request.getPhone().trim().isEmpty() && userRepository.existsByPhone(request.getPhone().trim())) {
                throw new DuplicateResourceException("Số điện thoại đã tồn tại trong hệ thống");
            }
            user = User.builder()
                    .fullName(request.getFullName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .password(passwordEncoder.encode(defaultPassword))
                    .role(targetRole)
                    .build();
        }
        user = userRepository.save(user);

        if (targetRole == Role.DEPARTMENT_MANAGER) {
            specialty.setManager(user);
            specialty = specialtyRepository.save(specialty);
        }

        Doctor doctor = doctorRepository.findByUserId(user.getId()).orElse(null);
        if (doctor == null) {
            doctor = Doctor.builder()
                    .user(user)
                    .specialty(specialty)
                    .consultationFee(request.getConsultationFee() != null ? request.getConsultationFee() : 0.0)
                    .biography(request.getBiography())
                    .active(true)
                    .build();
        } else {
            doctor.setSpecialty(specialty);
            if (request.getConsultationFee() != null) {
                doctor.setConsultationFee(request.getConsultationFee());
            }
            if (request.getBiography() != null) {
                doctor.setBiography(request.getBiography());
            }
            doctor.setActive(true);
        }

        return mapToResponse(
                doctorRepository.save(doctor)
        );
    }


    @Override
    public Page<DoctorResponse> getAllDoctors(Boolean active, int page, int size, String sortBy, String sortDir) {
        
        if ("fullName".equals(sortBy)) {
            sortBy = "user.fullName";
        } else if ("email".equals(sortBy)) {
            sortBy = "user.email";
        } else if ("phone".equals(sortBy)) {
            sortBy = "user.phone";
        }

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (active == null) {
            return doctorRepository.findAllDoctorsWithCorrectRole(pageable).map(this::mapToResponse);
        }
        
        return doctorRepository.findByActiveWithCorrectRole(active, pageable).map(this::mapToResponse);
    }


    @Override
    public Page<DoctorResponse> searchDoctors(String keyword, int page, int size, String sortBy, String sortDir) {
        if ("fullName".equals(sortBy)) {
            sortBy = "user.fullName";
        } else if ("email".equals(sortBy)) {
            sortBy = "user.email";
        } else if ("phone".equals(sortBy)) {
            sortBy = "user.phone";
        }

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (keyword == null || keyword.trim().isEmpty()) {
            return doctorRepository.findAllDoctorsWithCorrectRole(pageable).map(this::mapToResponse);
        }

        return doctorRepository.searchDoctorsWithCorrectRole(keyword.trim(), pageable)
                .map(this::mapToResponse);
    }

    @Override
    public DoctorResponse getDoctorById(Long id) {

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor not found")
                );

        return mapToResponse(doctor);
    }


    @Override
    public DoctorResponse updateDoctor(Long id, DoctorUpdateRequest request) {

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor not found")
                );


        User user = doctor.getUser();
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());

        if (request.getSpecialtyId() != null) {
            Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Specialty not found"));

            if (user.getRole() == Role.DEPARTMENT_MANAGER) {
                if (specialty.getManager() != null && !specialty.getManager().getId().equals(user.getId())) {
                    throw new DuplicateResourceException("Chuyên khoa " + specialty.getName() + " đã có Trưởng khoa (" + specialty.getManager().getFullName() + "). Mỗi chuyên khoa chỉ được có 1 Trưởng khoa.");
                }
                if (doctor.getSpecialty() != null && !doctor.getSpecialty().getId().equals(specialty.getId())) {
                    Specialty oldSpecialty = doctor.getSpecialty();
                    if (oldSpecialty.getManager() != null && oldSpecialty.getManager().getId().equals(user.getId())) {
                        oldSpecialty.setManager(null);
                        specialtyRepository.save(oldSpecialty);
                    }
                }
                specialty.setManager(user);
                specialtyRepository.save(specialty);
            }

            doctor.setSpecialty(specialty);
        }
        
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setBiography(request.getBiography());

        if (request.getActive() != null) {
            doctor.setActive(request.getActive());
            user.setEnabled(request.getActive());
        }

        userRepository.save(user);


        return mapToResponse(
                doctorRepository.save(doctor)
        );
    }


    @Override
    @Transactional
    public void deleteDoctor(Long id) {

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor not found")
                );

        if (appointmentRepository.existsByDoctorId(doctor.getId())) {
            throw new BadRequestException("Không thể xóa bác sĩ đã có lịch hẹn khám. Vui lòng sử dụng tính năng 'Tạm khóa' tài khoản thay vì xóa.");
        }

        User user = doctor.getUser();

        if (user != null && appointmentRepository.existsByPatientId(user.getId())) {
            throw new BadRequestException("Không thể xóa tài khoản do người dùng này có lịch sử đặt khám trong hệ thống. Vui lòng sử dụng tính năng 'Tạm khóa'.");
        }

        List<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctorId(doctor.getId());
        if (!schedules.isEmpty()) {
            for (DoctorSchedule schedule : schedules) {
                List<TimeSlot> slots = timeSlotRepository.findByDoctorScheduleId(schedule.getId());
                if (!slots.isEmpty()) {
                    timeSlotRepository.deleteAll(slots);
                }
            }
            doctorScheduleRepository.deleteAll(schedules);
        }

        if (user != null) {
            user.setDoctor(null);
            specialtyRepository.findByManagerId(user.getId()).ifPresent(s -> {
                s.setManager(null);
                specialtyRepository.save(s);
            });
        }

        doctor.setUser(null);
        doctor.setSpecialty(null);
        doctorRepository.delete(doctor);
        doctorRepository.flush();

        if (user != null) {
            userRepository.delete(user);
        }
    }



    private DoctorResponse mapToResponse(Doctor doctor){

        SpecialtyResponse specialtyResponse = null;
        if (doctor.getSpecialty() != null) {
            specialtyResponse = SpecialtyResponse.builder()
                    .id(doctor.getSpecialty().getId())
                    .name(doctor.getSpecialty().getName())
                    .description(doctor.getSpecialty().getDescription())
                    .active(doctor.getSpecialty().getActive())
                    .build();
        }

        return DoctorResponse.builder()
                .id(doctor.getId())
                .fullName(doctor.getUser().getFullName())
                .specialty(specialtyResponse)
                .consultationFee(doctor.getConsultationFee())
                .phone(doctor.getUser().getPhone())
                .email(doctor.getUser().getEmail())
                .biography(doctor.getBiography())
                .active(doctor.getActive())
                .role(doctor.getUser() != null ? doctor.getUser().getRole() : null)
                .createdAt(doctor.getCreatedAt())
                .build();
    }
}