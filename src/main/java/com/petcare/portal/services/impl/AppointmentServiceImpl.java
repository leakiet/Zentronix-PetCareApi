package com.petcare.portal.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petcare.portal.dtos.AppointmentDtos.AppointmentRequest;
import com.petcare.portal.dtos.AppointmentDtos.AppointmentResponse;
import com.petcare.portal.entities.Appointment;
import com.petcare.portal.entities.Pet;
import com.petcare.portal.entities.User;
import com.petcare.portal.enums.AppointmentStatus;
import com.petcare.portal.repositories.AppointmentRepository;
import com.petcare.portal.repositories.PetRepository;
import com.petcare.portal.repositories.UserRepository;
import com.petcare.portal.services.AppointmentService;
import com.petcare.portal.services.NotificationShelterService;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

  private static final Logger logger = LoggerFactory.getLogger(AppointmentServiceImpl.class);

  @Autowired
  private AppointmentRepository appointmentRepository;

  @Autowired
  private PetRepository petRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private NotificationShelterService notificationService;

  @Autowired
  private ModelMapper modelMapper;

  @Override
  public List<AppointmentResponse> getAppointmentsByOwnerId(Long ownerId) {
    try {
      List<Appointment> appointments = appointmentRepository.findByOwnerId(ownerId);
      return appointments.stream()
          .map(appointment -> modelMapper.map(appointment, AppointmentResponse.class))
          .collect(Collectors.toList());
    } catch (Exception e) {
      logger.error("Error getting appointments for owner {}: {}", ownerId, e.getMessage());
      throw new RuntimeException("Error getting appointments for owner: " + e.getMessage());
    }
  }

  @Override
  public AppointmentResponse createAppointment(AppointmentRequest request) {
    try {
        logger.info("Creating appointment with request: {}", request);
        
        Appointment appointment = new Appointment();
        
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setReason(request.getReason());
        appointment.setNotes(request.getNotes());
        appointment.setStatus(request.getStatus() != null ? request.getStatus() : AppointmentStatus.SCHEDULED);
        
        if (request.getOwnerId() != null) {
            User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Owner not found with ID: " + request.getOwnerId()));
            appointment.setOwner(owner);
        }
        
        if (request.getPetId() != null) {
            Pet pet = petRepository.findById(request.getPetId())
                .orElseThrow(() -> new RuntimeException("Pet not found with ID: " + request.getPetId()));
            appointment.setPet(pet);
        }
        
        if (request.getVetId() != null) {
            User vet = userRepository.findById(request.getVetId())
                .orElseThrow(() -> new RuntimeException("Vet not found with ID: " + request.getVetId()));
            appointment.setVet(vet);
        }
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        logger.info("Successfully created appointment with ID: {}", savedAppointment.getId());
        
        try {
            notificationService.sendNewAppointmentNotification(savedAppointment);
            logger.info("Sent notification to vet for new appointment: {}", savedAppointment.getId());
        } catch (Exception e) {
            logger.warn("Failed to send notification for appointment {}: {}", savedAppointment.getId(), e.getMessage());
        }
        
        return mapToResponse(savedAppointment);
    } catch (Exception e) {
      logger.error("Error creating appointment: {}", e.getMessage());
      throw new RuntimeException("Error creating appointment: " + e.getMessage());
    }
  }

  @Override
  public AppointmentResponse updateAppointment(Long id, AppointmentRequest request) {
    try {
      Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
      if (appointmentOpt.isEmpty()) {
        throw new RuntimeException("Appointment not found with ID: " + id);
      }

      Appointment appointment = appointmentOpt.get();

      // Check for conflicts if time is being changed
      if (request.getAppointmentTime() != null &&
          !request.getAppointmentTime().equals(appointment.getAppointmentTime())) {
        if (appointmentRepository.existsConflictingAppointment(appointment.getVet().getId(),
            request.getAppointmentTime())) {
          throw new RuntimeException("Conflicting appointment exists for the new time slot");
        }
        appointment.setAppointmentTime(request.getAppointmentTime());
      }

      if (request.getReason() != null) {
        appointment.setReason(request.getReason());
      }
      if (request.getNotes() != null) {
        appointment.setNotes(request.getNotes());
      }
      if (request.getStatus() != null) {
        appointment.setStatus(request.getStatus());
      }

      Appointment updatedAppointment = appointmentRepository.save(appointment);
      logger.info("Successfully updated appointment with ID: {}", id);

      return mapToResponse(updatedAppointment);
    } catch (Exception e) {
      logger.error("Error updating appointment with ID {}: {}", id, e.getMessage(), e);
      throw new RuntimeException("Error updating appointment: " + e.getMessage());
    }
  }

  @Override
  public AppointmentResponse getAppointmentById(Long id) {
    Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
    if (appointmentOpt.isEmpty()) {
      throw new RuntimeException("Appointment not found with ID: " + id);
    }
    return mapToResponse(appointmentOpt.get());
  }

  @Override
  public List<AppointmentResponse> getAppointmentsByVetId(Long vetId) {
    List<Appointment> appointments = appointmentRepository.findByVetId(vetId);
    return appointments.stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Override
  public List<AppointmentResponse> getUpcomingAppointmentsByVetId(Long vetId) {
    LocalDateTime now = LocalDateTime.now();
    List<Appointment> appointments = appointmentRepository.findByVetIdAndAppointmentTimeAfter(vetId, now);
    return appointments.stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Override
  public List<AppointmentResponse> getAppointmentsByPetId(Long petId) {
    List<Appointment> appointments = appointmentRepository.findByPetId(petId);
    return appointments.stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Override
  public void updateAppointmentStatus(Long id, AppointmentStatus status) {
    Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
    if (appointmentOpt.isEmpty()) {
      throw new RuntimeException("Appointment not found with ID: " + id);
    }

    Appointment appointment = appointmentOpt.get();
    appointment.setStatus(status);
    Appointment savedAppointment = appointmentRepository.save(appointment);

    // Gửi thông báo cho owner
    if (status == AppointmentStatus.COMPLETED) {
      notificationService.sendAppointmentCompletedNotification(savedAppointment);
    } else {
      notificationService.sendAppointmentStatusNotification(savedAppointment, status.toString());
    }

    logger.info("Updated appointment {} status to {}", id, status);
  }

  @Override
  public void deleteAppointment(Long id) {
    if (!appointmentRepository.existsById(id)) {
      throw new RuntimeException("Appointment not found with ID: " + id);
    }
    appointmentRepository.deleteById(id);
    logger.info("Successfully deleted appointment with ID: {}", id);
  }

  @Override
  public void rescheduleAppointment(Long id, String newAppointmentTime) {
    try {
      Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
      if (appointmentOpt.isEmpty()) {
        throw new RuntimeException("Appointment not found with ID: " + id);
      }

      Appointment appointment = appointmentOpt.get();
      LocalDateTime newDateTime = LocalDateTime.parse(newAppointmentTime);

      if (appointmentRepository.existsConflictingAppointment(appointment.getVet().getId(), newDateTime)) {
        throw new RuntimeException("Conflicting appointment exists for the new time slot");
      }

      appointment.setAppointmentTime(newDateTime);
      Appointment savedAppointment = appointmentRepository.save(appointment);

      notificationService.sendAppointmentRescheduleNotification(savedAppointment);

      logger.info("Successfully rescheduled appointment {} to {}", id, newAppointmentTime);
    } catch (Exception e) {
      logger.error("Error rescheduling appointment with ID {}: {}", id, e.getMessage(), e);
      throw new RuntimeException("Error rescheduling appointment: " + e.getMessage());
    }
  }

  private AppointmentResponse mapToResponse(Appointment appointment) {
    AppointmentResponse response = new AppointmentResponse();
    response.setId(appointment.getId());
    response.setPet(appointment.getPet());
    response.setOwner(appointment.getOwner());
    response.setVet(appointment.getVet());
    response.setAppointmentTime(appointment.getAppointmentTime());
    response.setReason(appointment.getReason());
    response.setNotes(appointment.getNotes());
    response.setStatus(appointment.getStatus());
    response.setCreatedAt(appointment.getCreatedAt());
    response.setUpdatedAt(appointment.getUpdatedAt());
    return response;
  }
}
