package com.petcare.portal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.petcare.portal.entities.Appointment;
import com.petcare.portal.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

  List<Appointment> findByVetId(Long vetId);

  List<Appointment> findByPetId(Long petId);

  List<Appointment> findByOwnerId(Long ownerId);

  List<Appointment> findByStatus(AppointmentStatus status);

  List<Appointment> findByVetIdAndAppointmentTimeAfter(Long vetId, LocalDateTime dateTime);

  List<Appointment> findByAppointmentTimeAfter(LocalDateTime dateTime);

  List<Appointment> findByVetIdAndAppointmentTimeBefore(Long vetId, LocalDateTime dateTime);

  List<Appointment> findByPetIdAndAppointmentTimeBefore(Long petId, LocalDateTime dateTime);

  List<Appointment> findByVetIdAndAppointmentTimeBetween(Long vetId, LocalDateTime start, LocalDateTime end);

  long countByVetIdAndStatus(Long vetId, AppointmentStatus status);

  long countByStatus(AppointmentStatus status);

  @Query("SELECT a FROM Appointment a " +
      "JOIN FETCH a.pet p " +
      "JOIN FETCH a.owner o " +
      "WHERE a.vet.id = :vetId " +
      "ORDER BY a.appointmentTime DESC")
  List<Appointment> findAppointmentsWithDetailsByVetId(@Param("vetId") Long vetId);

  @Query("SELECT COUNT(a) > 0 FROM Appointment a " +
      "WHERE a.vet.id = :vetId " +
      "AND a.appointmentTime = :appointmentTime " +
      "AND a.status != 'CANCELLED'")
  boolean existsConflictingAppointment(@Param("vetId") Long vetId,
      @Param("appointmentTime") LocalDateTime appointmentTime);
}
