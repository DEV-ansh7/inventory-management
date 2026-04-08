package com.inventory.service;

import com.inventory.entity.Client;
import com.inventory.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepo;

    public List<Client> getAll() {
        return clientRepo.findByActiveTrue();
    }

    public Client getById(Long id) {
        return clientRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found: " + id));
    }

    public Client create(Client client) {
        return clientRepo.save(client);
    }

    public Client update(Long id, Client updated) {
        Client existing = getById(id);
        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setAddress(updated.getAddress());
        existing.setUpiId(updated.getUpiId());
        return clientRepo.save(existing);
    }

    public void delete(Long id) {
        Client c = getById(id);
        c.setActive(false);
        clientRepo.save(c);
    }

    public List<Client> search(String name) {
        return clientRepo.findByNameContainingIgnoreCaseAndActiveTrue(name);
    }
}
