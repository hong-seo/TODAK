package com.todak.api.appointment.repository;

import com.todak.api.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /** ---------------------------------------------------------
     * 1) 특정 환자의 전체 예약
     * --------------------------------------------------------- */
    List<Appointment> findByPatientId(UUID patientId);


    /** ---------------------------------------------------------
     * 2) 환자의 오늘 예약
     * DATE(a.datetime) = :date 조건 사용
     * --------------------------------------------------------- */
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.patientId = :patientId
        AND DATE(a.datetime) = :date
    """)
    Optional<Appointment> findTodayAppointment(UUID patientId, LocalDate date);


    /** ---------------------------------------------------------
     * 3) 병원 전체 예약
     * --------------------------------------------------------- */
    List<Appointment> findByHospital_HospitalId(Long hospitalId);


    /** ---------------------------------------------------------
     * 4) 병원 특정 날짜 예약
     * --------------------------------------------------------- */
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.hospital.hospitalId = :hospitalId
        AND DATE(a.datetime) = :date
    """)
    List<Appointment> findByHospitalIdAndDate(Long hospitalId, LocalDate date);
}
