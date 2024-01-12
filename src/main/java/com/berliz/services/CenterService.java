package com.berliz.services;

import com.berliz.DTO.CenterRequest;
import com.berliz.DTO.PhotoAlbum;
import com.berliz.DTO.VideoAlbum;
import com.berliz.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * Service interface for managing center-related operations.
 */
public interface CenterService {

    ResponseEntity<String> addCenter(CenterRequest centerRequest) throws JsonProcessingException;

    ResponseEntity<List<Center>> getAllCenters();

    ResponseEntity<String> updateCenter(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> updateMyCenterTrainers(Map<String, String> requestMap);

    ResponseEntity<String> deleteCenter(Integer id) throws JsonProcessingException;

    ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException;

    ResponseEntity<Center> getCenter();

    ResponseEntity<Center> getByUserId(Integer id);

    ResponseEntity<String> likeCenter(Integer id) throws JsonProcessingException;

    ResponseEntity<List<CenterLike>> getCenterLikes();

    ResponseEntity<String> updatePhoto(CenterRequest centerRequest) throws JsonProcessingException;

    ResponseEntity<List<Center>> getActiveCenters();

    ResponseEntity<String> addCenterAnnouncement(Map<String, String> requestMap);

    ResponseEntity<String> updateCenterAnnouncement(Map<String, String> requestMap);

    ResponseEntity<String> deleteCenterAnnouncement(Integer id);

    ResponseEntity<String> updateCenterAnnouncementStatus(Integer id);

    ResponseEntity<List<CenterAnnouncement>> getAllCenterAnnouncements();

    ResponseEntity<List<CenterAnnouncement>> getMyCenterAnnouncements();

    ResponseEntity<String> addCenterEquipment(Map<String, String> requestMap);

    ResponseEntity<String> updateCenterEquipment(Map<String, String> requestMap);

    ResponseEntity<String> deleteCenterEquipment(Integer id);

    ResponseEntity<List<CenterEquipment>> getAllCenterEquipments();

    ResponseEntity<List<CenterEquipment>> getMyCenterEquipments();

    ResponseEntity<String> addCenterIntroduction(Map<String, String> requestMap);

    ResponseEntity<String> updateCenterIntroduction(Map<String, String> requestMap);

    ResponseEntity<String> deleteCenterIntroduction(Integer id);

    ResponseEntity<List<CenterIntroduction>> getAllCenterIntroductions();

    ResponseEntity<List<CenterIntroduction>> getMyCenterIntroductions();

    ResponseEntity<String> addCenterLocation(Map<String, String> requestMap);

    ResponseEntity<String> updateCenterLocation(Map<String, String> requestMap);

    ResponseEntity<String> deleteCenterLocation(Integer id);

    ResponseEntity<List<CenterLocation>> getAllCenterLocations();

    ResponseEntity<List<CenterLocation>> getMyCenterLocations();

    ResponseEntity<String> addCenterPhotoAlbum(PhotoAlbum photoAlbum);

    ResponseEntity<String> updateCenterPhotoAlbum(PhotoAlbum photoAlbum);

    ResponseEntity<String> deleteCenterPhotoAlbum(Integer id);

    ResponseEntity<List<CenterPhotoAlbum>> getAllCenterPhotoAlbums();

    ResponseEntity<List<CenterPhotoAlbum>> getMyCenterPhotoAlbums();

    ResponseEntity<String> addCenterPricing(Map<String, String> requestMap);

    ResponseEntity<String> updateCenterPricing(Map<String, String> requestMap);

    ResponseEntity<String> deleteCenterPricing(Integer id);

    ResponseEntity<List<CenterPricing>> getAllCenterPricing();

    ResponseEntity<List<CenterPricing>> getMyCenterPricing();

    ResponseEntity<String> addCenterTrainer(Map<String, String> requestMap);

    ResponseEntity<String> updateCenterTrainer(Map<String, String> requestMap);

    ResponseEntity<String> deleteCenterTraining(Integer id);

    ResponseEntity<List<CenterTrainer>> getAllCenterTrainers();

    ResponseEntity<List<CenterTrainer>> getMyCenterTrainers();

    ResponseEntity<String> addCenterVideoAlbum(VideoAlbum videoAlbum);

    ResponseEntity<String> updateCenterVideoAlbum(VideoAlbum videoAlbum);

    ResponseEntity<String> deleteCenterVideoAlbum(Integer id);

    ResponseEntity<List<CenterVideoAlbum>> getAllCenterVideoAlbums();

    ResponseEntity<List<CenterVideoAlbum>> getMyCenterVideoAlbums();


}
