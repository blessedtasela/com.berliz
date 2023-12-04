package com.berliz.services;

import com.berliz.models.Client;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface ClientService {
    ResponseEntity<String> addClient(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<List<Client>> getAllClients();

    ResponseEntity<List<Client>> getActiveClients();

    ResponseEntity<String> updateClient(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> deleteClient(Integer id) throws JsonProcessingException;

    ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException;

    ResponseEntity<Client> getClient(Integer id);
}
