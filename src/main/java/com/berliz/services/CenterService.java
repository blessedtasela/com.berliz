package com.berliz.services;

import com.berliz.DTO.*;
import com.berliz.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

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

    ResponseEntity<String> addCenterAnnouncement(AnnouncementRequest announcementRequest) throws JsonProcessingException;

    ResponseEntity<String> updateCenterAnnouncement(AnnouncementRequest announcementRequest) throws JsonProcessingException;

    ResponseEntity<String> deleteCenterAnnouncement(Integer id) throws JsonProcessingException;

    ResponseEntity<String> updateCenterAnnouncementStatus(Integer id) throws JsonProcessingException;

    ResponseEntity<List<CenterAnnouncement>> getAllCenterAnnouncements();

    ResponseEntity<List<CenterAnnouncement>> getMyCenterAnnouncements();

    ResponseEntity<List<CenterAnnouncement>> getActiveCenterAnnouncements(Integer id);

    ResponseEntity<String> addCenterEquipment(EquipmentRequest equipmentRequest) throws JsonProcessingException;

    ResponseEntity<String> updateCenterEquipment(EquipmentRequest equipmentRequest) throws JsonProcessingException;

    ResponseEntity<String> deleteCenterEquipment(Integer id) throws JsonProcessingException;

    ResponseEntity<List<CenterEquipment>> getAllCenterEquipments();

    ResponseEntity<List<CenterEquipment>> getMyCenterEquipments();

    ResponseEntity<String> addCenterIntroduction(IntroductionRequest introductionRequest) throws JsonProcessingException;

    ResponseEntity<String> updateCenterIntroduction(IntroductionRequest introductionRequest) throws JsonProcessingException;

    ResponseEntity<String> deleteCenterIntroduction(Integer id) throws JsonProcessingException;

    ResponseEntity<List<CenterIntroduction>> getAllCenterIntroductions();

    ResponseEntity<List<CenterIntroduction>> getMyCenterIntroductions();

    ResponseEntity<String> addCenterLocation(LocationRequest locationRequest) throws JsonProcessingException;

    ResponseEntity<String> updateCenterLocation(LocationRequest locationRequest) throws JsonProcessingException;

    ResponseEntity<String> deleteCenterLocation(Integer id) throws JsonProcessingException;

    ResponseEntity<List<CenterLocation>> getAllCenterLocations();

    ResponseEntity<List<CenterLocation>> getMyCenterLocations();

    ResponseEntity<String> addCenterPhotoAlbum(PhotoAlbumRequest photoAlbumRequest) throws JsonProcessingException;

    ResponseEntity<String> updateCenterPhotoAlbum(PhotoAlbumRequest photoAlbumRequest) throws JsonProcessingException;

    ResponseEntity<String> deleteCenterPhotoAlbum(Integer id) throws JsonProcessingException;

    ResponseEntity<List<CenterPhotoAlbum>> getAllCenterPhotoAlbums();

    ResponseEntity<List<CenterPhotoAlbum>> getMyCenterPhotoAlbums();

    ResponseEntity<String> addCenterPricing(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> updateCenterPricing(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> deleteCenterPricing(Integer id) throws JsonProcessingException;

    ResponseEntity<List<CenterPricing>> getAllCenterPricing();

    ResponseEntity<CenterPricing> getMyCenterPricing();

    ResponseEntity<String> addCenterTrainer(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> updateCenterTrainerStatus(Integer id) throws JsonProcessingException;

    ResponseEntity<String> deleteCenterTrainer(Integer id) throws JsonProcessingException;

    ResponseEntity<List<CenterTrainer>> getAllCenterTrainers();

    ResponseEntity<List<CenterTrainer>> getMyCenterTrainers();

    ResponseEntity<String> addCenterVideoAlbum(VideoAlbumRequest videoAlbumRequest) throws JsonProcessingException;

    ResponseEntity<String> updateCenterVideoAlbum(VideoAlbumRequest videoAlbumRequest) throws JsonProcessingException;

    ResponseEntity<String> deleteCenterVideoAlbum(Integer id) throws JsonProcessingException;

    ResponseEntity<List<CenterVideoAlbum>> getAllCenterVideoAlbums();

    ResponseEntity<List<CenterVideoAlbum>> getMyCenterVideoAlbums();


    ResponseEntity<String> addCenterReview(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> updateCenterReview(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> updateCenterReviewStatus(Integer id) throws JsonProcessingException;

    ResponseEntity<String> disableCenterReview(Integer id) throws JsonProcessingException;

    ResponseEntity<String> deleteCenterReview(Integer id) throws JsonProcessingException;

    ResponseEntity<List<CenterReview>> getMyCenterReviews();

    ResponseEntity<List<CenterReview>> getAllCenterReviews();

    ResponseEntity<List<CenterReview>> getActiveCenterReviews(Integer id);
}
