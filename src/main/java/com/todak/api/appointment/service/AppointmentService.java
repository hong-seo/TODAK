package com.todak.api.appointment.service;

import com.todak.api.appointment.dto.request.AppointmentCreateRequestDto;
import com.todak.api.appointment.dto.request.AppointmentCancelRequest;
import com.todak.api.appointment.dto.response.AppointmentResponseDto;

import java.util.List;
import java.util.UUID;

public interface AppointmentService {

    // 1. 예약 생성 (환자)
    AppointmentResponseDto create(AppointmentCreateRequestDto request);

    // 2. 예약 취소 (환자)
    void cancel(AppointmentCancelRequest request);

    // 3. 나의 모든 예약 조회
    List<AppointmentResponseDto> getMyAppointments(UUID patientId);

    // 4. 오늘 나의 예약 조회
    AppointmentResponseDto getTodayMyAppointment(UUID patientId);

    // 5. 병원 전체 예약 조회
    List<AppointmentResponseDto> getAppointmentsByHospital(Long hospitalId);

    // 6. 병원 특정 날짜 예약 조회
    List<AppointmentResponseDto> getAppointmentsByHospitalAndDate(Long hospitalId, String date);
}
