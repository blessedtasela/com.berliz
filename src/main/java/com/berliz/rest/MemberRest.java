
package com.berliz.rest;

import com.berliz.models.CenterReview;
import com.berliz.models.Client;
import com.berliz.models.Member;
import com.berliz.models.TrainerReview;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API endpoints for managing member-related operations.
 */
@RequestMapping(path = "/member")
public interface MemberRest {

    @PostMapping(path = "/add")
    ResponseEntity<String> addMember(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    @GetMapping(path = "/get")
    ResponseEntity<List<Member>> getAllMembers();

    @GetMapping(path = "/getActiveMembers")
    ResponseEntity<List<Member>> getActiveMembers();

    @PutMapping(path = "/update")
    ResponseEntity<String> updateMember(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteMember(@PathVariable Integer id) throws JsonProcessingException;

    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getMember")
    ResponseEntity<Member> getMember(Integer id);

    @GetMapping(path = "/getMyCenterReviews")
    ResponseEntity<List<CenterReview>> getMyCenterReviews();

    @GetMapping(path = "/getMyMembers")
    ResponseEntity<List<Member>> getMyMembers();

    @GetMapping(path = "/getMyActiveMembers")
    ResponseEntity<List<Member>> getMyActiveMembers();
}