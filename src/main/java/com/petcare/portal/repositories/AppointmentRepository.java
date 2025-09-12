package com.petcare.portal.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.petcare.portal.entities.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
	 List<Appointment> findByOwnerId(Long ownerId);
	 List<Appointment> findByVetId(Long vetId);
}
