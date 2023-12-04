package com.berliz.services;

import com.berliz.models.Member;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface MemberService {
    public ResponseEntity<String> addMember(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<List<Member>> getAllMembers();

    ResponseEntity<List<Member>> getActiveMembers();

    ResponseEntity<String> updateMember(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> deleteMember(Integer id) throws JsonProcessingException;

    ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException;

    ResponseEntity<Member> getMember(Integer id);
}
