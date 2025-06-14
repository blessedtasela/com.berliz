package com.berliz.services;

import com.berliz.DTO.*;
import com.berliz.models.*;
import com.berliz.models.TrainerReview;
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

    ResponseEntity<List<TrainerPricing>> getAllTrainerPricing();

    ResponseEntity<TrainerPricing> getMyTrainerPricing();

    ResponseEntity<String> deleteTrainerPricing(Integer id) throws JsonProcessingException;

    ResponseEntity<String> addTrainerPhotoAlbum(PhotoAlbumRequest photoAlbumRequest) throws JsonProcessingException;

    ResponseEntity<String> updateTrainerPhotoAlbum(PhotoAlbumRequest photoAlbumRequest) throws JsonProcessingException;

    ResponseEntity<String> deleteTrainerPhotoAlbum(Integer id) throws JsonProcessingException;

    ResponseEntity<List<TrainerPhotoAlbum>> getAllTrainerPhotoAlbums();

    ResponseEntity<List<TrainerPhotoAlbumResponse>> getAllTrainerPhotoAlbumsWithPhotos();

    ResponseEntity<TrainerPhotoAlbumResponse> getMyTrainerPhotoAlbum(Integer albumId);

    ResponseEntity<List<Photo>> getTrainerPhotoAlbumPhotos();

    ResponseEntity<String> addTrainerBenefit(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> updateTrainerBenefit(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> deleteTrainerBenefit(Integer id) throws JsonProcessingException;

    ResponseEntity<List<TrainerBenefit>> getAllTrainerBenefits();

    ResponseEntity<TrainerBenefit> getMyTrainerBenefit();

    ResponseEntity<String> addTrainerIntroduction(@ModelAttribute IntroductionRequest introductionRequest) throws JsonProcessingException;

    ResponseEntity<String> updateTrainerIntroduction(@ModelAttribute IntroductionRequest introductionRequest) throws JsonProcessingException;

    ResponseEntity<String> deleteTrainerIntroduction(Integer id) throws JsonProcessingException;

    ResponseEntity<List<TrainerIntroduction>> getAllTrainerIntroductions();

    ResponseEntity<TrainerIntroduction> getMyTrainerIntroduction();

    ResponseEntity<String> addTrainerVideoAlbum(VideoAlbumRequest videoAlbumRequest) throws JsonProcessingException;

    ResponseEntity<String> updateTrainerVideoAlbum(VideoAlbumRequest videoAlbumRequest) throws JsonProcessingException;

    ResponseEntity<String> deleteTrainerVideoAlbum(Integer id) throws JsonProcessingException;

    ResponseEntity<List<TrainerVideoAlbum>> getAllTrainerVideoAlbums();

    ResponseEntity<List<TrainerVideoAlbum>> getMyTrainerVideoAlbums();

    ResponseEntity<List<TrainerReview>> getAllTrainerReviews();

    ResponseEntity<List<TrainerReview>> getActiveTrainerReviews(Integer id);

    ResponseEntity<List<Client>> getMyClients();

    ResponseEntity<List<Client>> getMyActiveClients();

    ResponseEntity<String> addTrainerFeatureVideo(FeatureVideoRequest featureVideoRequest) throws JsonProcessingException;

    ResponseEntity<String> updateTrainerFeatureVideo(FeatureVideoRequest featureVideoRequest) throws JsonProcessingException;

    ResponseEntity<String> deleteTrainerFeatureVideo(Integer id) throws JsonProcessingException;

    ResponseEntity<List<TrainerFeatureVideo>> getAllTrainerFeatureVideos();

    ResponseEntity<TrainerFeatureVideo> getMyTrainerFeatureVideo();

    ResponseEntity<String> likeTrainerReview(Integer id) throws JsonProcessingException;

    ResponseEntity<List<TrainerReviewLike>> getTrainerReviewLikes();

    ResponseEntity<List<CenterTrainer>> getMyCenterTrainers();

    ResponseEntity<String> addTrainerReview(TrainerReviewRequest trainerReviewRequest) throws JsonProcessingException;

    ResponseEntity<String> updateTrainerReview(TrainerReviewRequest trainerReviewRequest) throws JsonProcessingException;

    ResponseEntity<String> updateTrainerReviewStatus(Integer id) throws JsonProcessingException;

    ResponseEntity<String> disableTrainerReview(Integer id) throws JsonProcessingException;

    ResponseEntity<String> deleteTrainerReview(Integer id) throws JsonProcessingException;

    ResponseEntity<List<TrainerReview>> getMyTrainerReviews();


}
