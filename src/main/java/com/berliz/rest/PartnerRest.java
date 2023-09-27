package com.berliz.rest;

import com.berliz.DTO.PartnerRequest;
import com.berliz.models.Partner;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/partner")
public interface PartnerRest {


    @PostMapping(path = "/add")
    ResponseEntity<String> addPartner(@ModelAttribute PartnerRequest request);

    @GetMapping(path = "/get")
    ResponseEntity<List<Partner>> getAllPartners();

    @PutMapping(path = "/update")
    ResponseEntity<String> updatePartner(@RequestBody Map<String, String> requestMap);

    @PutMapping(path = "/updateFile")
    ResponseEntity<String> updateFile(@ModelAttribute PartnerRequest request );

    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deletePartner(@PathVariable Integer id);

    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id);

    @PutMapping(path = "/reject/{id}")
    ResponseEntity<String> rejectApplication(@PathVariable Integer id);

    @GetMapping(path = "/getPartner")
    ResponseEntity<Partner> getPartner();

}
