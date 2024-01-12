package com.berliz.restImplement;

import com.berliz.DTO.*;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.*;
import com.berliz.rest.TrainerRest;
import com.berliz.services.TrainerService;
import com.berliz.utils.BerlizUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * REST API endpoints implementation for Trainer-related operations.
 */
@RestController
public class TrainerRestImplement implements TrainerRest {

    @Autowired
    TrainerService trainerService;

    @Override
    public ResponseEntity<String> addTrainer(TrainerRequest trainerRequest) throws JsonProcessingException {
        try {
            return trainerService.addTrainer(trainerRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<Trainer>> getAllTrainers() {
        try {
            return trainerService.getAllTrainers();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Trainer>> getActiveTrainers() {
        try {
            return trainerService.getActiveTrainers();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateTrainer(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return trainerService.updateTrainer(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updatePhoto(TrainerRequest trainerRequest) throws JsonProcessingException {
        try {
            return trainerService.updatePhoto(trainerRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteTrainer(Integer id) throws JsonProcessingException {
        try {
            return trainerService.deleteTrainer(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException {
        try {
            return trainerService.updateStatus(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<Trainer> getTrainer() {
        try {
            return trainerService.getTrainer();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Trainer(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> likeTrainer(Integer id) throws JsonProcessingException {
        try {
            return trainerService.likeTrainer(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<TrainerLike>> getTrainerLikes() throws JsonProcessingException {
        try {
            return trainerService.getTrainerLikes();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addTrainerPricing(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return trainerService.addTrainerPricing(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateTrainerPricing(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return trainerService.updateTrainerPricing(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<TrainerPricing>> getTrainerPricing() {
        try {
            return trainerService.getTrainerPricing();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteTrainerPricing(Integer id) throws JsonProcessingException {
        try {
            return trainerService.deleteTrainerPricing(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> addTrainerPhotoAlbum(PhotoAlbum photoAlbum) throws JsonProcessingException {
        try {
            return trainerService.addTrainerPhotoAlbum(photoAlbum);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateTrainerPhotoAlbum(PhotoAlbum photoAlbum) throws JsonProcessingException {
        try {
            return trainerService.updateTrainerPhotoAlbum(photoAlbum);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteTrainerPhotoAlbum(Integer id) throws JsonProcessingException {
        try {
            return trainerService.deleteTrainerPhotoAlbum(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<TrainerPhotoAlbum>> getAllTrainerPhotoAlbums() {
        try {
            return trainerService.getAllTrainerPhotoAlbums();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<TrainerPhotoAlbum>> getMyTrainerPhotoAlbums() {
        try {
            return trainerService.getMyTrainerPhotoAlbums();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addTrainerBenefit(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return trainerService.addTrainerBenefit(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateTrainerBenefit(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return trainerService.updateTrainerBenefit(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteTrainerBenefit(Integer id) throws JsonProcessingException {
        try {
            return trainerService.deleteTrainerBenefit(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<TrainerBenefit>> getAllTrainerBenefits() {
        try {
            return trainerService.getAllTrainerBenefits();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<TrainerBenefit>> getMyTrainerBenefits() {
        try {
            return trainerService.getMyTrainerBenefits();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addTrainerIntroduction(Introduction introduction) throws JsonProcessingException {
        try {
            return trainerService.addTrainerIntroduction(introduction);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateTrainerIntroduction( Introduction introduction) throws JsonProcessingException {
        try {
            return trainerService.updateTrainerIntroduction(introduction);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteTrainerIntroduction(Integer id) throws JsonProcessingException {
        try {
            return trainerService.deleteTrainerIntroduction(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<TrainerIntroduction>> getAllTrainerIntroductions() {
        try {
            return trainerService.getAllTrainerIntroductions();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<TrainerIntroduction> getMyTrainerIntroduction() {
        try {
            return trainerService.getMyTrainerIntroduction();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new TrainerIntroduction(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addTrainerVideoAlbum(VideoAlbum videoAlbum) throws JsonProcessingException {
        try {
            return trainerService.addTrainerVideoAlbum(videoAlbum);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateTrainerVideoAlbum(VideoAlbum videoAlbum) throws JsonProcessingException {
        try {
            return trainerService.updateTrainerVideoAlbum(videoAlbum);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteTrainerVideoAlbum(Integer id) throws JsonProcessingException {
        try {
            return trainerService.deleteTrainerVideoAlbum(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<TrainerVideoAlbum>> getAllTrainerVideoAlbums() {
        try {
            return trainerService.getAllTrainerVideoAlbums();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<TrainerVideoAlbum>> getMyTrainerVideoAlbums() {
        try {
            return trainerService.getMyTrainerVideoAlbums();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addTrainerClientReview(ClientReview clientReview) throws JsonProcessingException {
        try {
            return trainerService.addTrainerClientReview(clientReview);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateTrainerClientReview(ClientReview clientReview) throws JsonProcessingException {
        try {
            return trainerService.updateTrainerClientReview(clientReview);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteTrainerClientReview(Integer id) throws JsonProcessingException {
        try {
            return trainerService.deleteTrainerClientReview(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<TrainerClientReview>> getAllTrainerClientReviews() {
        try {
            return trainerService.getAllTrainerClientReviews();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<TrainerClientReview>> getActiveTrainerClientReviews() {
        try {
            return trainerService.getActiveTrainerClientReviews();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<TrainerClientReview>> getMyTrainerClientReviews() {
        try {
            return trainerService.getMyTrainerClientReviews();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addTrainerFeatureVideo(FeatureVideo featureVideo) throws JsonProcessingException {
        try {
            return trainerService.addTrainerFeatureVideo(featureVideo);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateTrainerFeatureVideo(FeatureVideo featureVideo) throws JsonProcessingException {
        try {
            return trainerService.updateTrainerFeatureVideo(featureVideo);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteTrainerFeatureVideo(Integer id) throws JsonProcessingException {
        try {
            return trainerService.deleteTrainerFeatureVideo(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<TrainerFeatureVideo>> getAllTrainerFeatureVideos() {
        try {
            return trainerService.getAllTrainerFeatureVideos();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<TrainerFeatureVideo>> getMyTrainerFeatureVideos() {
        try {
            return trainerService.getMyTrainerFeatureVideos();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
