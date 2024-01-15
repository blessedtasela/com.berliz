
package com.berliz.rest;

import com.berliz.models.Center;
import com.berliz.models.Member;
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

    /**
     * Update an existing member's details.
     *
     * @param requestMap Request body containing updated member details.
     * @return ResponseEntity indicating the result of the member update operation.
     */
    @PutMapping(path = "/update")
    ResponseEntity<String> updateMember(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    /**
     * Delete a member.
     *
     * @param id The ID of the member to delete.
     * @return ResponseEntity indicating the result of the member deletion operation.
     */
    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteMember(@PathVariable Integer id) throws JsonProcessingException;

    /**
     * Update the status of a member.
     *
     * @param id The ID of the member to update.
     * @return ResponseEntity indicating the result of the member status update operation.
     */
    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id) throws JsonProcessingException;

    /**
     * Get a member.
     *
     * @return ResponseEntity containing the member with the specified ID.
     */
    @GetMapping(path = "/getMember")
    ResponseEntity<Member> getMember(Integer id);

}