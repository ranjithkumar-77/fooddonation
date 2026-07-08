package com.ranjith.fooddonation.repository;

import com.ranjith.fooddonation.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DonationRepository extends JpaRepository<Donation, Long> {

    List<Donation> findByDonorName(String donorName);

    List<Donation> findByStatus(String status);

    Optional<Donation> findById(Long id);

    long countByStatus(String status);
}