package com.youmed.service;

import com.youmed.dto.request.RejectScheduleRequest;
import com.youmed.dto.response.DoctorResponse;
import com.youmed.dto.response.DoctorScheduleResponse;
import java.util.List;

public interface DepartmentManagerService {
    List<DoctorResponse> getManagedDoctors();
    List<DoctorScheduleResponse> getManagedSchedules();
    DoctorScheduleResponse approveSchedule(Long scheduleId);
    DoctorScheduleResponse rejectSchedule(Long scheduleId, RejectScheduleRequest request);
}
