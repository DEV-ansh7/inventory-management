package com.inventory.repository;

import com.inventory.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByActiveTrue();
    List<Client> findByNameContainingIgnoreCaseAndActiveTrue(String name);
    boolean existsByEmail(String email);
}
