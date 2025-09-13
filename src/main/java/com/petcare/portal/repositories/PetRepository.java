package com.petcare.portal.repositories;

import com.petcare.portal.entities.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.petcare.portal.entities.User;
import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
  Pet findById(long id);
  List<Pet> findByOwner(User owner);
  List<Pet> findByOwnerAndIsDeletedFalse(User owner);
  boolean existsByImageAndIsDeletedFalse(String image);
}
