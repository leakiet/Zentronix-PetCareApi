package com.petcare.portal.controllers;

import com.petcare.portal.dtos.AppointmentDtos.AppointmentRequest;
import com.petcare.portal.dtos.AppointmentDtos.AppointmentResponse;
import com.petcare.portal.entities.NotificationMessage;
import com.petcare.portal.enums.AppointmentStatus;
import com.petcare.portal.services.AppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/apis/v1/appointments")
public class AppointmentController {

  private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);

  @Autowired
  private AppointmentService appointmentService;

  @PostMapping
  public ResponseEntity<AppointmentResponse> createAppointment(@RequestBody AppointmentRequest request) {
    try {
      logger.info("Creating appointment: {}", request);
      AppointmentResponse response = appointmentService.createAppointment(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (Exception e) {
      logger.error("Error creating appointment: {}", e.getMessage());
      return ResponseEntity.badRequest().body(null);
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<AppointmentResponse> updateAppointment(
      @PathVariable Long id,
      @RequestBody AppointmentRequest request) {
    try {
      logger.info("Updating appointment {}: {}", id, request);
      AppointmentResponse response = appointmentService.updateAppointment(id, request);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      logger.error("Error updating appointment: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable Long id) {
    try {
      AppointmentResponse response = appointmentService.getAppointmentById(id);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      logger.error("Error getting appointment {}: {}", id, e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/vet/{vetId}")
  public ResponseEntity<List<AppointmentResponse>> getAppointmentsByVetId(@PathVariable Long vetId) {
    try {
      List<AppointmentResponse> appointments = appointmentService.getAppointmentsByVetId(vetId);
      return ResponseEntity.ok(appointments);
    } catch (Exception e) {
      logger.error("Error getting appointments for vet {}: {}", vetId, e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/vet/{vetId}/upcoming")
  public ResponseEntity<List<AppointmentResponse>> getUpcomingAppointmentsByVetId(@PathVariable Long vetId) {
    try {
      List<AppointmentResponse> appointments = appointmentService.getUpcomingAppointmentsByVetId(vetId);
      return ResponseEntity.ok(appointments);
    } catch (Exception e) {
      logger.error("Error getting upcoming appointments for vet {}: {}", vetId, e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/pet/{petId}")
  public ResponseEntity<List<AppointmentResponse>> getAppointmentsByPetId(@PathVariable Long petId) {
    try {
      List<AppointmentResponse> appointments = appointmentService.getAppointmentsByPetId(petId);
      return ResponseEntity.ok(appointments);
    } catch (Exception e) {
      logger.error("Error getting appointments for pet {}: {}", petId, e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/notifications/{userId}")
  public ResponseEntity<List<NotificationMessage>> getAppointmentNotifications(@PathVariable Long userId) {
    try {
      logger.info("Getting appointment notifications for user {}", userId);
      
      List<AppointmentResponse> appointments;
      try {
        appointments = appointmentService.getAppointmentsByOwnerId(userId);
        logger.info("Found {} appointments as owner for user {}", appointments.size(), userId);
      } catch (Exception e) {
        logger.info("No appointments found as owner, trying as vet for user {}", userId);
        appointments = appointmentService.getAppointmentsByVetId(userId);
        logger.info("Found {} appointments as vet for user {}", appointments.size(), userId);
      }
      
      List<NotificationMessage> notifications = appointments.stream()
          .map(appointment -> {
            NotificationMessage notification = new NotificationMessage();
            notification.setType("APPOINTMENT_UPDATE");
            notification.setMessage(String.format("Appointment with %s - %s", 
                appointment.getPet() != null ? appointment.getPet().getPetName() : "Pet", 
                appointment.getStatus()));
            notification.setAppointmentId(appointment.getId());
            return notification;
          })
          .collect(Collectors.toList());
      
      logger.info("Returning {} notifications for user {}", notifications.size(), userId);
      return ResponseEntity.ok(notifications);
    } catch (Exception e) {
      logger.error("Error getting appointment notifications for user {}: {}", userId, e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  @PutMapping("/{id}/status")
  public ResponseEntity<Map<String, String>> updateAppointmentStatus(
      @PathVariable Long id,
      @RequestParam AppointmentStatus status) {
    try {
      logger.info("Updating appointment {} status to {}", id, status);
      appointmentService.updateAppointmentStatus(id, status);
      return ResponseEntity.ok(Map.of("message", "Appointment status updated successfully"));
    } catch (Exception e) {
      logger.error("Error updating appointment status: {}", e.getMessage());
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, String>> deleteAppointment(@PathVariable Long id) {
    try {
      logger.info("Deleting appointment {}", id);
      appointmentService.deleteAppointment(id);
      return ResponseEntity.ok(Map.of("message", "Appointment deleted successfully"));
    } catch (Exception e) {
      logger.error("Error deleting appointment: {}", e.getMessage());
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @PutMapping("/{id}/reschedule")
  public ResponseEntity<Map<String, String>> rescheduleAppointment(
      @PathVariable Long id,
      @RequestBody Map<String, String> request) {
    try {
      String newAppointmentTime = request.get("appointmentTime");
      logger.info("Rescheduling appointment {} to {}", id, newAppointmentTime);
      appointmentService.rescheduleAppointment(id, newAppointmentTime);
      return ResponseEntity.ok(Map.of("message", "Appointment rescheduled successfully"));
    } catch (Exception e) {
      logger.error("Error rescheduling appointment: {}", e.getMessage());
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }
  
}
