package com.todak.api.appointment.service;

import com.todak.api.appointment.dto.request.AppointmentCreateRequestDto;
import com.todak.api.appointment.dto.request.AppointmentCancelRequest;
import com.todak.api.appointment.dto.response.AppointmentResponseDto;
import com.todak.api.appointment.entity.Appointment;
import com.todak.api.appointment.entity.AppointmentStatus;
import com.todak.api.appointment.repository.AppointmentRepository;
import com.todak.api.hospital.entity.Hospital;
import com.todak.api.hospital.entity.Doctor;
import com.todak.api.hospital.repository.DepartmentRepository;
import com.todak.api.hospital.repository.DoctorRepository;
import com.todak.api.hospital.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final HospitalRepository hospitalRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;

    /** 1. 예약 생성 */
    @Override
    public AppointmentResponseDto create(AppointmentCreateRequestDto request) {

        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new IllegalArgumentException("hospital not found"));

        Doctor doctor = null;
        if (request.getDoctorId() != null) {
            doctor = doctorRepository.findById(request.getDoctorId())
                    .orElse(null);
        }

        Appointment appointment = Appointment.builder()
                .patientId(UUID.fromString(request.getPatientId()))
                .hospital(hospital)
                .doctor(doctor)
                .datetime(request.getDatetime().atOffset(ZoneOffset.of("+09:00")))
                .status(AppointmentStatus.REQUESTED)
                .build();

        appointmentRepository.save(appointment);

        return AppointmentResponseDto.from(appointment);
    }

    /** 2. 예약 취소 */
    @Override
    public void cancel(AppointmentCancelRequest request) {

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new IllegalArgumentException("appointment not found"));

        if (!appointment.getPatientId().equals(request.getPatientId())) {
            throw new IllegalArgumentException("invalid patient");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    /** 3. 나의 전체 예약 */
    @Override
    public List<AppointmentResponseDto> getMyAppointments(UUID patientId) {
        return appointmentRepository.findByPatientId(patientId)
                .stream()
                .map(AppointmentResponseDto::from)
                .toList();
    }

    /** 4. 오늘 나의 예약 */
    @Override
    public AppointmentResponseDto getTodayMyAppointment(UUID patientId) {
        OffsetDateTime now = OffsetDateTime.now();

        Appointment appt = appointmentRepository
                .findTodayAppointment(patientId, now.toLocalDate())
                .orElse(null);

        return appt != null ? AppointmentResponseDto.from(appt) : null;
    }

    /** 5. 병원 전체 예약 */
    @Override
    public List<AppointmentResponseDto> getAppointmentsByHospital(Long hospitalId) {
        return appointmentRepository.findByHospital_HospitalId(hospitalId)
                .stream()
                .map(AppointmentResponseDto::from)
                .toList();
    }

    /** 6. 병원 특정 날짜 예약 */
    @Override
    public List<AppointmentResponseDto> getAppointmentsByHospitalAndDate(Long hospitalId, String dateStr) {

        OffsetDateTime date = OffsetDateTime.parse(
                dateStr + "T00:00:00+09:00",
                DateTimeFormatter.ISO_OFFSET_DATE_TIME
        );

        return appointmentRepository.findByHospitalIdAndDate(hospitalId, date.toLocalDate())
                .stream()
                .map(AppointmentResponseDto::from)
                .toList();
    }
}
