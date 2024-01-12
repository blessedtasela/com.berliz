package com.berliz.services;

import com.berliz.DTO.*;
import com.berliz.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Map;

/**
 * Service interface for managing trainer-related operations.
 */
public interface TrainerService {

    ResponseEntity<String> addTrainer(TrainerRequest trainerRequest) throws JsonProcessingException;

    ResponseEntity<List<Trainer>> getAllTrainers();

    ResponseEntity<String> updateTrainer(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> updatePhoto(TrainerRequest trainerRequest) throws JsonProcessingException;

    ResponseEntity<String> deleteTrainer(Integer id) throws JsonProcessingException;

    ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException;

    ResponseEntity<Trainer> getTrainer();

    ResponseEntity<List<Trainer>> getActiveTrainers();

    ResponseEntity<String> likeTrainer(Integer id) throws JsonProcessingException;

    ResponseEntity<List<TrainerLike>> getTrainerLikes();

    ResponseEntity<String> addTrainerPricing(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> updateTrainerPricing(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<List<TrainerPricing>> getTrainerPricing();

    ResponseEntity<String> deleteTrainerPricing(Integer id) throws JsonProcessingException;

    ResponseEntity<String> addTrainerPhotoAlbum(PhotoAlbum photoAlbum) throws JsonProcessingException;

    ResponseEntity<String> updateTrainerPhotoAlbum(PhotoAlbum photoAlbum) throws JsonProcessingException;

    ResponseEntity<String> deleteTrainerPhotoAlbum(Integer id) throws JsonProcessingException;

    ResponseEntity<List<TrainerPhotoAlbum>> getAllTrainerPhotoAlbums();

    ResponseEntity<List<TrainerPhotoAlbum>> getMyTrainerPhotoAlbums();

    ResponseEntity<String> addTrainerBenefit(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> updateTrainerBenefit(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> deleteTrainerBenefit(Integer id) throws JsonProcessingException;

    ResponseEntity<List<TrainerBenefit>> getAllTrainerBenefits();

    ResponseEntity<List<TrainerBenefit>> getMyTrainerBenefits();

    ResponseEntity<String> addTrainerIntroduction(@ModelAttribute Introduction introduction) throws JsonProcessingException;

    ResponseEntity<String> updateTrainerIntroduction(@ModelAttribute Introduction introduction) throws JsonProcessingException;

    ResponseEntity<String> deleteTrainerIntroduction(Integer id) throws JsonProcessingException;

    ResponseEntity<List<TrainerIntroduction>> getAllTrainerIntroductions();

    ResponseEntity<TrainerIntroduction> getMyTrainerIntroduction();

    ResponseEntity<String> addTrainerVideoAlbum(VideoAlbum videoAlbum) throws JsonProcessingException;

    ResponseEntity<String> updateTrainerVideoAlbum(VideoAlbum videoAlbum) throws JsonProcessingException;

    ResponseEntity<String> deleteTrainerVideoAlbum(Integer id) throws JsonProcessingException;

    ResponseEntity<List<TrainerVideoAlbum>> getAllTrainerVideoAlbums();

    ResponseEntity<List<TrainerVideoAlbum>> getMyTrainerVideoAlbums();

    ResponseEntity<String> addTrainerClientReview(ClientReview clientReview);

    ResponseEntity<String> updateTrainerClientReview(ClientReview clientReview);

    ResponseEntity<String> deleteTrainerClientReview(Integer id);

    ResponseEntity<List<TrainerClientReview>> getAllTrainerClientReviews();

    ResponseEntity<List<TrainerClientReview>> getActiveTrainerClientReviews();

    ResponseEntity<List<TrainerClientReview>> getMyTrainerClientReviews();

    ResponseEntity<String> addTrainerFeatureVideo(FeatureVideo featureVideo);

    ResponseEntity<String> updateTrainerFeatureVideo(FeatureVideo featureVideo);

    ResponseEntity<String> deleteTrainerFeatureVideo(Integer id);

    ResponseEntity<List<TrainerFeatureVideo>> getAllTrainerFeatureVideos();

    ResponseEntity<List<TrainerFeatureVideo>> getMyTrainerFeatureVideos();
}
