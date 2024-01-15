package com.berliz.restImplement;

import com.berliz.DTO.*;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.*;
import com.berliz.models.ClientReview;
import com.berliz.rest.TrainerRest;
import com.berliz.services.TrainerService;
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
    public ResponseEntity<List<TrainerPricing>> getAllTrainerPricing() {
        try {
            return trainerService.getAllTrainerPricing();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<TrainerPricing> getMyTrainerPricing() {
        try {
            return trainerService.getMyTrainerPricing();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new TrainerPricing(), HttpStatus.INTERNAL_SERVER_ERROR);
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
    public ResponseEntity<String> addTrainerPhotoAlbum(PhotoAlbumRequest photoAlbumRequest) throws JsonProcessingException {
        try {
            return trainerService.addTrainerPhotoAlbum(photoAlbumRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateTrainerPhotoAlbum(PhotoAlbumRequest photoAlbumRequest) throws JsonProcessingException {
        try {
            return trainerService.updateTrainerPhotoAlbum(photoAlbumRequest);
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
    public ResponseEntity<String> addTrainerIntroduction(IntroductionRequest introductionRequest) throws JsonProcessingException {
        try {
            return trainerService.addTrainerIntroduction(introductionRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateTrainerIntroduction( IntroductionRequest introductionRequest) throws JsonProcessingException {
        try {
            return trainerService.updateTrainerIntroduction(introductionRequest);
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
    public ResponseEntity<String> addTrainerVideoAlbum(VideoAlbumRequest videoAlbumRequest) throws JsonProcessingException {
        try {
            return trainerService.addTrainerVideoAlbum(videoAlbumRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateTrainerVideoAlbum(VideoAlbumRequest videoAlbumRequest) throws JsonProcessingException {
        try {
            return trainerService.updateTrainerVideoAlbum(videoAlbumRequest);
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
    public ResponseEntity<String> disableClientReview(Integer id) throws JsonProcessingException {
        try {
            return trainerService.disableClientReview(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<ClientReview>> getMyClientReviews() {
        try {
            return trainerService.getMyClientReviews();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<ClientReview>> getAllClientReviews() {
        try {
            return trainerService.getAllClientReviews();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    public ResponseEntity<List<ClientReview>> getActiveClientReviews(Integer id) {
        try {
            return trainerService.getActiveClientReviews(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addTrainerFeatureVideo(FeatureVideoRequest featureVideoRequest) throws JsonProcessingException {
        try {
            return trainerService.addTrainerFeatureVideo(featureVideoRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateTrainerFeatureVideo(FeatureVideoRequest featureVideoRequest) throws JsonProcessingException {
        try {
            return trainerService.updateTrainerFeatureVideo(featureVideoRequest);
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

    @Override
    public ResponseEntity<String> likeClientReview(Integer id) throws JsonProcessingException {
        try {
            return trainerService.likeClientReview(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<ClientReviewLike>> getClientReviewLikes() throws JsonProcessingException {
        try {
            return trainerService.getClientReviewLikes();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CenterTrainer>> getMyCenterTrainers() throws JsonProcessingException {
        try {
            return trainerService.getMyCenterTrainers();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
