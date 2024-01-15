package com.berliz.rest;

import com.berliz.DTO.*;
import com.berliz.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API endpoints for managing center-related operations.
 */
@RequestMapping(path = "/center")
public interface CenterRest {

    @PostMapping(path = "/add")
    ResponseEntity<String> addCenter(@ModelAttribute CenterRequest centerRequest) throws JsonProcessingException;

    @GetMapping(path = "/get")
    ResponseEntity<List<Center>> getAllCenters();

    @GetMapping(path = "/getActiveCenters")
    ResponseEntity<List<Center>> getActiveCenters();

    @PutMapping(path = "/update")
    ResponseEntity<String> updateCenter(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    @PutMapping(path = "/updateMyCenterTrainers")
    ResponseEntity<String> updateMyCenterTrainers(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteCenter(@PathVariable Integer id) throws JsonProcessingException;

    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getByUserId")
    ResponseEntity<Center> getByUserId(@PathVariable Integer id);

    @GetMapping(path = "/getCenter")
    ResponseEntity<Center> getCenter();

    @PutMapping(path = "/like/{id}")
    ResponseEntity<String> likeCenter(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getCenterLikes")
    ResponseEntity<List<CenterLike>> getCenterLikes() throws JsonProcessingException;

    @PutMapping(path = "/updatePhoto")
    ResponseEntity<String> updatePhoto(@ModelAttribute CenterRequest centerRequest) throws JsonProcessingException;

    @PostMapping(path = "/addCenterAnnouncement")
    ResponseEntity<String> addCenterAnnouncement(@ModelAttribute AnnouncementRequest announcementRequest) throws JsonProcessingException;

    @PutMapping(path = "/updateCenterAnnouncement")
    ResponseEntity<String> updateCenterAnnouncement(@ModelAttribute AnnouncementRequest announcementRequest) throws JsonProcessingException;

    @DeleteMapping(path = "/deleteCenterAnnouncement/{id}")
    ResponseEntity<String> deleteCenterAnnouncement(@PathVariable Integer id) throws JsonProcessingException;

    @PutMapping(path = "/updateCenterAnnouncementStatus/{id}")
    ResponseEntity<String> updateCenterAnnouncementStatus(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getAllCenterAnnouncements")
    ResponseEntity<List<CenterAnnouncement>> getAllCenterAnnouncements();

    @GetMapping(path = "/getMyCenterAnnouncements")
    ResponseEntity<List<CenterAnnouncement>> getMyCenterAnnouncements();

    @GetMapping(path = "/getActiveCenterAnnouncements/{id}")
    ResponseEntity<List<CenterAnnouncement>> getActiveCenterAnnouncements(@PathVariable Integer id);

    @PutMapping(path = "/addCenterEquipment")
    ResponseEntity<String> addCenterEquipment(@ModelAttribute EquipmentRequest equipmentRequest) throws JsonProcessingException;

    @PutMapping(path = "/updateCenterEquipment")
    ResponseEntity<String> updateCenterEquipment(@ModelAttribute EquipmentRequest equipmentRequest) throws JsonProcessingException;

    @DeleteMapping(path = "/deleteCenterEquipment/{id}")
    ResponseEntity<String> deleteCenterEquipment(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getAllCenterEquipments")
    ResponseEntity<List<CenterEquipment>> getAllCenterEquipments();

    @GetMapping(path = "/getMyCenterEquipments")
    ResponseEntity<List<CenterEquipment>> getMyCenterEquipments();

    @PostMapping(path = "/addCenterIntroduction")
    ResponseEntity<String> addCenterIntroduction(@ModelAttribute IntroductionRequest introductionRequest) throws JsonProcessingException;

    @PutMapping(path = "/updateCenterIntroduction")
    ResponseEntity<String> updateCenterIntroduction(@ModelAttribute IntroductionRequest introductionRequest) throws JsonProcessingException;

    @DeleteMapping(path = "/deleteCenterIntroduction/{id}")
    ResponseEntity<String> deleteCenterIntroduction(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getAllCenterIntroductions")
    ResponseEntity<List<CenterIntroduction>> getAllCenterIntroductions();

    @GetMapping(path = "/getMyCenterIntroductions")
    ResponseEntity<List<CenterIntroduction>> getMyCenterIntroductions();

    @PostMapping(path = "/addCenterLocation")
    ResponseEntity<String> addCenterLocation(@ModelAttribute LocationRequest locationRequest) throws JsonProcessingException;

    @PutMapping(path = "/updateCenterLocation")
    ResponseEntity<String> updateCenterLocation(@ModelAttribute LocationRequest locationRequest) throws JsonProcessingException;

    @DeleteMapping(path = "/deleteCenterLocation/{id}")
    ResponseEntity<String> deleteCenterLocation(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getAllCenterLocations")
    ResponseEntity<List<CenterLocation>> getAllCenterLocations();

    @GetMapping(path = "/getMyCenterLocations")
    ResponseEntity<List<CenterLocation>> getMyCenterLocations();

    @PutMapping(path = "/addCenterPhotoAlbum")
    ResponseEntity<String> addCenterPhotoAlbum(@ModelAttribute PhotoAlbumRequest photoAlbumRequest) throws JsonProcessingException;

    @PutMapping(path = "/updateCenterPhotoAlbum")
    ResponseEntity<String> updateCenterPhotoAlbum(@ModelAttribute PhotoAlbumRequest photoAlbumRequest) throws JsonProcessingException;

    @DeleteMapping(path = "/deleteCenterPhotoAlbum/{id}")
    ResponseEntity<String> deleteCenterPhotoAlbum(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getAllCenterPhotoAlbums")
    ResponseEntity<List<CenterPhotoAlbum>> getAllCenterPhotoAlbums();

    @GetMapping(path = "/getMyCenterPhotoAlbums")
    ResponseEntity<List<CenterPhotoAlbum>> getMyCenterPhotoAlbums();

    @PutMapping(path = "/addCenterPricing")
    ResponseEntity<String> addCenterPricing(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    @PutMapping(path = "/updateCenterPricing")
    ResponseEntity<String> updateCenterPricing(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    @DeleteMapping(path = "/deleteCenterPricing/{id}")
    ResponseEntity<String> deleteCenterPricing(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getAllCenterPricing")
    ResponseEntity<List<CenterPricing>> getAllCenterPricing();

    @GetMapping(path = "/getMyCenterPricing")
    ResponseEntity<CenterPricing> getMyCenterPricing();

    @PutMapping(path = "/addCenterTrainer")
    ResponseEntity<String> addCenterTrainer(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    @PutMapping(path = "/updateCenterTrainerStatus/{id}")
    ResponseEntity<String> updateCenterTrainerStatus(@PathVariable Integer id) throws JsonProcessingException;

    @DeleteMapping(path = "/deleteCenterTrainer/{id}")
    ResponseEntity<String> deleteCenterTrainer(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getAllCenterTrainers")
    ResponseEntity<List<CenterTrainer>> getAllCenterTrainers();

    @GetMapping(path = "/getMyCenterTrainers")
    ResponseEntity<List<CenterTrainer>> getMyCenterTrainers();

    @PostMapping(path = "/addCenterVideoAlbum")
    ResponseEntity<String> addCenterVideoAlbum(@ModelAttribute VideoAlbumRequest videoAlbumRequest) throws JsonProcessingException;

    @PutMapping(path = "/updateCenterVideoAlbum")
    ResponseEntity<String> updateCenterVideoAlbum(@ModelAttribute VideoAlbumRequest videoAlbumRequest) throws JsonProcessingException;

    @DeleteMapping(path = "/deleteCenterVideoAlbum/{id}")
    ResponseEntity<String> deleteCenterVideoAlbum(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getAllCenterVideoAlbums")
    ResponseEntity<List<CenterVideoAlbum>> getAllCenterVideoAlbums();

    @GetMapping(path = "/getMyCenterVideoAlbums")
    ResponseEntity<List<CenterVideoAlbum>> getMyCenterVideoAlbums();
}
