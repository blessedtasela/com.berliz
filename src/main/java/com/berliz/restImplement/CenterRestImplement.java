package com.berliz.restImplement;

import com.berliz.DTO.*;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.*;
import com.berliz.rest.CenterRest;
import com.berliz.services.CenterService;
import com.berliz.utils.BerlizUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * REST API endpoints implementation for center-related operations.
 */
@RestController
public class CenterRestImplement implements CenterRest {

    @Autowired
    CenterService centerService;

    @Override
    public ResponseEntity<String> addCenter(CenterRequest centerRequest) throws JsonProcessingException {
        try {
            return centerService.addCenter(centerRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<Center>> getAllCenters() {
        try {
            return centerService.getAllCenters();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Center>> getActiveCenters() {
        try {
            return centerService.getActiveCenters();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Override
    public ResponseEntity<String> updateCenter(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return centerService.updateCenter(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateMyCenterTrainers(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return centerService.updateMyCenterTrainers(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteCenter(Integer id) throws JsonProcessingException {
        try {
            return centerService.deleteCenter(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException {
        try {
            return centerService.updateStatus(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<Center> getByUserId(Integer id) {
        try {
            // Delegate the center getByUserId operation to the service
            return centerService.getByUserId(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Center(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Center> getCenter() {
        try {
            // Delegate the center getCenter operation to the service
            return centerService.getCenter();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Center(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> likeCenter(Integer id) throws JsonProcessingException {
        try {
            return centerService.likeCenter(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<CenterLike>> getCenterLikes() {
        try {
            return centerService.getCenterLikes();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updatePhoto(CenterRequest centerRequest) throws JsonProcessingException {
        try {
            return centerService.updatePhoto(centerRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> addCenterAnnouncement(AnnouncementRequest announcementRequest) throws JsonProcessingException {
        try {
            return centerService.addCenterAnnouncement(announcementRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateCenterAnnouncement(AnnouncementRequest announcementRequest) throws JsonProcessingException {
        try {
            return centerService.updateCenterAnnouncement(announcementRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteCenterAnnouncement(Integer id) throws JsonProcessingException {
        try {
            return centerService.deleteCenterAnnouncement(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateCenterAnnouncementStatus(Integer id) throws JsonProcessingException {
        try {
            return centerService.updateCenterAnnouncementStatus(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<CenterAnnouncement>> getAllCenterAnnouncements() {
        try {
            return centerService.getAllCenterAnnouncements();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CenterAnnouncement>> getMyCenterAnnouncements() {
        try {
            return centerService.getMyCenterAnnouncements();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CenterAnnouncement>> getActiveCenterAnnouncements(Integer id) {
        try {
            return centerService.getActiveCenterAnnouncements(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addCenterEquipment(EquipmentRequest equipmentRequest) throws JsonProcessingException {
        try {
            return centerService.addCenterEquipment(equipmentRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateCenterEquipment(EquipmentRequest equipmentRequest) throws JsonProcessingException {
        try {
            return centerService.updateCenterEquipment(equipmentRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteCenterEquipment(Integer id) throws JsonProcessingException {
        try {
            return centerService.deleteCenterEquipment(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<CenterEquipment>> getAllCenterEquipments() {
        try {
            return centerService.getAllCenterEquipments();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CenterEquipment>> getMyCenterEquipments() {
        try {
            return centerService.getMyCenterEquipments();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addCenterIntroduction(IntroductionRequest introductionRequest) throws JsonProcessingException {
        try {
            return centerService.addCenterIntroduction(introductionRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateCenterIntroduction(IntroductionRequest introductionRequest) throws JsonProcessingException {
        try {
            return centerService.updateCenterIntroduction(introductionRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteCenterIntroduction(Integer id) throws JsonProcessingException {
        try {
            return centerService.deleteCenterIntroduction(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<CenterIntroduction>> getAllCenterIntroductions() {
        try {
            return centerService.getAllCenterIntroductions();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CenterIntroduction>> getMyCenterIntroductions() {
        try {
            return centerService.getMyCenterIntroductions();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addCenterLocation(LocationRequest locationRequest) throws JsonProcessingException {
        try {
            return centerService.addCenterLocation(locationRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateCenterLocation(LocationRequest locationRequest) throws JsonProcessingException {
        try {
            return centerService.updateCenterLocation(locationRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteCenterLocation(Integer id) throws JsonProcessingException {
        try {
            return centerService.deleteCenterLocation(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<CenterLocation>> getAllCenterLocations() {
        try {
            return centerService.getAllCenterLocations();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CenterLocation>> getMyCenterLocations() {
        try {
            return centerService.getMyCenterLocations();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addCenterPhotoAlbum(PhotoAlbumRequest photoAlbumRequest) throws JsonProcessingException {
        try {
            return centerService.addCenterPhotoAlbum(photoAlbumRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateCenterPhotoAlbum(PhotoAlbumRequest photoAlbumRequest) throws JsonProcessingException {
        try {
            return centerService.updateCenterPhotoAlbum(photoAlbumRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteCenterPhotoAlbum(Integer id) throws JsonProcessingException {
        try {
            return centerService.deleteCenterPhotoAlbum(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<CenterPhotoAlbum>> getAllCenterPhotoAlbums() {
        try {
            return centerService.getAllCenterPhotoAlbums();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CenterPhotoAlbum>> getMyCenterPhotoAlbums() {
        try {
            return centerService.getMyCenterPhotoAlbums();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addCenterPricing(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return centerService.addCenterPricing(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateCenterPricing(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return centerService.updateCenterPricing(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteCenterPricing(Integer id) throws JsonProcessingException {
        try {
            return centerService.deleteCenterPricing(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<CenterPricing>> getAllCenterPricing() {
        try {
            return centerService.getAllCenterPricing();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<CenterPricing> getMyCenterPricing() {
        try {
            return centerService.getMyCenterPricing();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new CenterPricing(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addCenterTrainer(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return centerService.addCenterTrainer(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateCenterTrainerStatus(Integer id) throws JsonProcessingException {
        try {
            return centerService.updateCenterTrainerStatus(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteCenterTrainer(Integer id) throws JsonProcessingException {
        try {
            return centerService.deleteCenterTrainer(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<CenterTrainer>> getAllCenterTrainers() {
        try {
            return centerService.getAllCenterTrainers();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CenterTrainer>> getMyCenterTrainers() {
        try {
            return centerService.getMyCenterTrainers();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addCenterVideoAlbum(VideoAlbumRequest videoAlbumRequest) throws JsonProcessingException {
        try {
            return centerService.addCenterVideoAlbum(videoAlbumRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateCenterVideoAlbum(VideoAlbumRequest videoAlbumRequest) throws JsonProcessingException {
        try {
            return centerService.updateCenterVideoAlbum(videoAlbumRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteCenterVideoAlbum(Integer id) throws JsonProcessingException {
        try {
            return centerService.deleteCenterVideoAlbum(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<CenterVideoAlbum>> getAllCenterVideoAlbums() {
        try {
            return centerService.getAllCenterVideoAlbums();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CenterVideoAlbum>> getMyCenterVideoAlbums() {
        try {
            return centerService.getMyCenterVideoAlbums();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
