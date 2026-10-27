package com.galilstay.server.repository;

import com.galilstay.server.model.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest, Long> {

    boolean existsByEmailIgnoreCase(String email);
}
