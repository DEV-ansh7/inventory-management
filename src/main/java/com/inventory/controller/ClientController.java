package com.inventory.controller;

import com.inventory.dto.ApiResponse;
import com.inventory.entity.Client;
import com.inventory.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Client>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(clientService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Client>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(clientService.getById(id)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Client>>> search(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.ok(clientService.search(q)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Client>> create(@RequestBody Client client) {
        return ResponseEntity.ok(ApiResponse.ok("Client created", clientService.create(client)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Client>> update(@PathVariable Long id, @RequestBody Client client) {
        return ResponseEntity.ok(ApiResponse.ok("Client updated", clientService.update(id, client)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        clientService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Client deleted", null));
    }
}
