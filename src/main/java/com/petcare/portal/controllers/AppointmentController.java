package com.petcare.portal.controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.petcare.portal.dtos.UserResponseDto;
import com.petcare.portal.dtos.AppointmentDto.AppointmentRequestDto;
import com.petcare.portal.dtos.AppointmentDto.AppointmentResponseDto;
import com.petcare.portal.entities.User;
import com.petcare.portal.services.AppointmentService;
import com.petcare.portal.services.UserService;

@RestController
@RequestMapping("/apis/v1/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<AppointmentResponseDto> createAppointment(
            @RequestBody AppointmentRequestDto request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.badRequest().body(null);
        }
        User currentUser = userService.findByEmail(principal.getName());
        if (currentUser == null) {
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(appointmentService.createAppointment(request, currentUser));
    }

    @GetMapping("/owner")
    public ResponseEntity<List<AppointmentResponseDto>> getOwnerAppointments(Principal principal) {
        if (principal == null) {
            return ResponseEntity.badRequest().body(null);
        }
        User currentUser = userService.findByEmail(principal.getName());
        if (currentUser == null) {
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(appointmentService.getAppointmentsForOwner(currentUser));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<AppointmentResponseDto>> getAppointmentsByOwnerId(
            @PathVariable("ownerId") Long ownerId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.badRequest().body(null);
        }
        User currentUser = userService.findByEmail(principal.getName());
        if (currentUser == null) {
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(appointmentService.getAppointmentsForOwner(currentUser));
    }

    @GetMapping("/vet")
    public ResponseEntity<List<AppointmentResponseDto>> getVetAppointments(Principal principal) {
        if (principal == null) {
            return ResponseEntity.badRequest().body(null);
        }
        User currentUser = userService.findByEmail(principal.getName());
        if (currentUser == null) {
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(appointmentService.getAppointmentsForVet(currentUser));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AppointmentResponseDto> updateStatus(
            @PathVariable("id") Long id, @RequestParam("status") String status, Principal principal) {
        if (principal == null) {
            return ResponseEntity.badRequest().body(null);
        }
        User currentUser = userService.findByEmail(principal.getName());
        if (currentUser == null) {
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(appointmentService.updateAppointmentStatus(id, status, currentUser));
    }

    @GetMapping("/vets/suggested")
    public ResponseEntity<List<UserResponseDto>> suggestVets(
            @RequestParam("petId") Long petId, @RequestParam(value = "reason", required = false) String reason, Principal principal) {
        if (principal == null) {
            return ResponseEntity.badRequest().body(null);
        }
        User currentUser = userService.findByEmail(principal.getName());
        if (currentUser == null) {
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(appointmentService.suggestVets(petId, reason, currentUser));
    }
}