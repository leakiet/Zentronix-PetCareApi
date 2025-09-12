package com.petcare.portal.services.impl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petcare.portal.dtos.UserResponseDto;
import com.petcare.portal.dtos.AppointmentDto.AppointmentRequestDto;
import com.petcare.portal.dtos.AppointmentDto.AppointmentResponseDto;
import com.petcare.portal.entities.Appointment;
import com.petcare.portal.entities.Pet;
import com.petcare.portal.entities.User;
import com.petcare.portal.enums.AppointmentStatus;
import com.petcare.portal.enums.Role;
import com.petcare.portal.repositories.AppointmentRepository;
import com.petcare.portal.repositories.PetRepository;
import com.petcare.portal.repositories.UserRepository;
import com.petcare.portal.services.AppointmentService;

@Service
public class AppointmentServiceImpl implements AppointmentService {

 @Autowired
 private AppointmentRepository appointmentRepository;

 @Autowired
 private PetRepository petRepository;

 @Autowired
 private UserRepository userRepository;

 @Override
 public AppointmentResponseDto createAppointment(AppointmentRequestDto request, User currentUser) {
     Pet pet = petRepository.findById(request.getPetId())
             .orElseThrow(() -> new RuntimeException("Pet not found"));

     if (!pet.getOwner().getId().equals(currentUser.getId())) {
         throw new RuntimeException("You do not own this pet");
     }

     User vet = userRepository.findById(request.getVetId())
             .orElseThrow(() -> new RuntimeException("Vet not found"));

     if (vet.getRole() != Role.VET) {
         throw new RuntimeException("Selected user is not a vet");
     }

     // TODO: Check vet availability at the requested time

     Appointment appointment = new Appointment();
     appointment.setPet(pet);
     appointment.setOwner(currentUser);
     appointment.setVet(vet);
     appointment.setAppointmentTime(new com.google.api.client.util.DateTime(request.getAppointmentTime().toInstant(null).toEpochMilli()));
     appointment.setReason(request.getReason());
     appointment.setNotes(request.getNotes());
     appointment.setStatus(AppointmentStatus.SCHEDULED);

     Appointment saved = appointmentRepository.save(appointment);

     return mapToResponse(saved);
 }

 @Override
 public List<AppointmentResponseDto> getAppointmentsForOwner(User currentUser) {
     List<Appointment> appointments = appointmentRepository.findByOwnerId(currentUser.getId());
     return appointments.stream().map(this::mapToResponse).collect(Collectors.toList());
 }

 @Override
 public List<AppointmentResponseDto> getAppointmentsForVet(User currentUser) {
     if (currentUser.getRole() != Role.VET) {
         throw new RuntimeException("Not a vet");
     }
     List<Appointment> appointments = appointmentRepository.findByVetId(currentUser.getId());
     return appointments.stream().map(this::mapToResponse).collect(Collectors.toList());
 }

 @Override
 public AppointmentResponseDto updateAppointmentStatus(Long id, String status, User currentUser) {
     Appointment appointment = appointmentRepository.findById(id)
             .orElseThrow(() -> new RuntimeException("Appointment not found"));

     if (!appointment.getVet().getId().equals(currentUser.getId())) {
         throw new RuntimeException("Not your appointment to update");
     }

     try {
         appointment.setStatus(AppointmentStatus.valueOf(status.toUpperCase()));
     } catch (IllegalArgumentException e) {
         throw new RuntimeException("Invalid status");
     }

     Appointment updated = appointmentRepository.save(appointment);
     return mapToResponse(updated);
 }

 @Override
 public List<UserResponseDto> suggestVets(Long petId, String reason, User currentUser) {
     Pet pet = petRepository.findById(petId)
             .orElseThrow(() -> new RuntimeException("Pet not found"));

     if (!pet.getOwner().getId().equals(currentUser.getId())) {
         throw new RuntimeException("You do not own this pet");
     }

     List<User> vets = userRepository.findByRole(Role.VET);

     // Suggest based on location (distance)
     double userLat = currentUser.getAddress().getLatitude();
     double userLon = currentUser.getAddress().getLongitude();

     // TODO: Could filter based on reason if vets had specialties

     return vets.stream()
             .sorted(Comparator.comparingDouble(vet -> distance(userLat, userLon, vet.getAddress().getLatitude(), vet.getAddress().getLongitude())))
             .limit(5) // Top 5 closest
             .map(this::mapToUserResponse)
             .collect(Collectors.toList());
 }

 private double distance(double lat1, double lon1, double lat2, double lon2) {
     final int R = 6371; // Radius of the Earth in km
     double latDistance = Math.toRadians(lat2 - lat1);
     double lonDistance = Math.toRadians(lon2 - lon1);
     double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
             + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
             * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
     double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
     return R * c;
 }

 private AppointmentResponseDto mapToResponse(Appointment appointment) {
     AppointmentResponseDto dto = new AppointmentResponseDto();
     dto.setId(appointment.getId());
     dto.setAppointmentTime(appointment.getAppointmentTime() != null ? LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(appointment.getAppointmentTime().getValue()), java.time.ZoneId.systemDefault()) : null);
     dto.setReason(appointment.getReason());
     dto.setNotes(appointment.getNotes());
     dto.setStatus(appointment.getStatus().name());
     dto.setPetId(appointment.getPet().getId());
     dto.setPetName(appointment.getPet().getPetName());
     dto.setOwnerId(appointment.getOwner().getId());
     dto.setOwnerName(appointment.getOwner().getFirstName() + " " + appointment.getOwner().getLastName());
     dto.setVetId(appointment.getVet().getId());
     dto.setVetName(appointment.getVet().getFirstName() + " " + appointment.getVet().getLastName());
     return dto;
 }

 private UserResponseDto mapToUserResponse(User user) {
	    UserResponseDto dto = new UserResponseDto();
	    dto.setId(user.getId());
	    dto.setFirstName(user.getFirstName());
	    dto.setLastName(user.getLastName());
	    dto.setEmail(user.getEmail());
	    dto.setPhone(user.getPhone());
	    if (user.getAddress() != null) {
	        dto.setCity(user.getAddress().getCity());
	        dto.setLatitude(user.getAddress().getLatitude());
	        dto.setLongitude(user.getAddress().getLongitude());
	    }
	    return dto;
	}
}
