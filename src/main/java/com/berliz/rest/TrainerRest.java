package com.berliz.rest;

import com.berliz.DTO.*;
import com.berliz.models.*;
import com.berliz.models.ClientReview;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API endpoints for managing trainer-related operations.
 */
@RequestMapping(path = "/trainer")
public interface TrainerRest {

    @PostMapping(path = "/add")
    ResponseEntity<String> addTrainer(@ModelAttribute TrainerRequest trainerRequest) throws JsonProcessingException;

    @GetMapping(path = "/get")
    ResponseEntity<List<Trainer>> getAllTrainers();

    @GetMapping(path = "/getActiveTrainers")
    ResponseEntity<List<Trainer>> getActiveTrainers();

    @PutMapping(path = "/update")
    ResponseEntity<String> updateTrainer(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    @PutMapping(path = "/updatePhoto")
    ResponseEntity<String> updatePhoto(@ModelAttribute TrainerRequest trainerRequest) throws JsonProcessingException;

    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteTrainer(@PathVariable Integer id) throws JsonProcessingException;

    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getTrainer")
    ResponseEntity<Trainer> getTrainer();

    @PutMapping(path = "/like/{id}")
    ResponseEntity<String> likeTrainer(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getTrainerLikes")
    ResponseEntity<List<TrainerLike>> getTrainerLikes() throws JsonProcessingException;

    @PostMapping(path = "/addTrainerPricing")
    ResponseEntity<String> addTrainerPricing(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    @PutMapping(path = "/updateTrainerPricing")
    ResponseEntity<String> updateTrainerPricing(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    @GetMapping(path = "/getAllTrainerPricing")
    ResponseEntity<List<TrainerPricing>> getAllTrainerPricing();

    @GetMapping(path = "/getMyTrainerPricing")
    ResponseEntity<TrainerPricing> getMyTrainerPricing();

    @DeleteMapping(path = "/deleteTrainerPricing/{id}")
    ResponseEntity<String> deleteTrainerPricing(@PathVariable Integer id) throws JsonProcessingException;

    @PutMapping(path = "/addTrainerPhotoAlbum")
    ResponseEntity<String> addTrainerPhotoAlbum(@ModelAttribute PhotoAlbumRequest photoAlbumRequest) throws JsonProcessingException;

    @PutMapping(path = "/updateTrainerPhotoAlbum")
    ResponseEntity<String> updateTrainerPhotoAlbum(@ModelAttribute PhotoAlbumRequest photoAlbumRequest) throws JsonProcessingException;

    @DeleteMapping(path = "/deleteTrainerPhotoAlbum/{id}")
    ResponseEntity<String> deleteTrainerPhotoAlbum(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getAllTrainerPhotoAlbums")
    ResponseEntity<List<TrainerPhotoAlbum>> getAllTrainerPhotoAlbums();

    @GetMapping(path = "/getMyTrainerPhotoAlbums")
    ResponseEntity<List<TrainerPhotoAlbum>> getMyTrainerPhotoAlbums();

    @PutMapping(path = "/addTrainerBenefit")
    ResponseEntity<String> addTrainerBenefit(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    @PutMapping(path = "/updateTrainerBenefit")
    ResponseEntity<String> updateTrainerBenefit(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    @DeleteMapping(path = "/deleteTrainerBenefit/{id}")
    ResponseEntity<String> deleteTrainerBenefit(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getAllTrainerBenefits")
    ResponseEntity<List<TrainerBenefit>> getAllTrainerBenefits();

    @GetMapping(path = "/getMyTrainerBenefits")
    ResponseEntity<List<TrainerBenefit>> getMyTrainerBenefits();

    @PutMapping(path = "/addTrainerIntroduction")
    ResponseEntity<String> addTrainerIntroduction(@ModelAttribute IntroductionRequest introductionRequest) throws JsonProcessingException;

    @PutMapping(path = "/updateTrainerIntroduction")
    ResponseEntity<String> updateTrainerIntroduction(@ModelAttribute IntroductionRequest introductionRequest) throws JsonProcessingException;

    @DeleteMapping(path = "/deleteTrainerIntroduction/{id}")
    ResponseEntity<String> deleteTrainerIntroduction(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getAllTrainerIntroductions")
    ResponseEntity<List<TrainerIntroduction>> getAllTrainerIntroductions();

    @GetMapping(path = "/getMyTrainerIntroduction")
    ResponseEntity<TrainerIntroduction> getMyTrainerIntroduction();

    @PostMapping(path = "/addTrainerVideoAlbum")
    ResponseEntity<String> addTrainerVideoAlbum(@ModelAttribute VideoAlbumRequest videoAlbumRequest) throws JsonProcessingException;

    @PutMapping(path = "/updateTrainerVideoAlbum")
    ResponseEntity<String> updateTrainerVideoAlbum(@ModelAttribute VideoAlbumRequest videoAlbumRequest) throws JsonProcessingException;

    @DeleteMapping(path = "/deleteTrainerVideoAlbum/{id}")
    ResponseEntity<String> deleteTrainerVideoAlbum(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getAllTrainerVideoAlbums")
    ResponseEntity<List<TrainerVideoAlbum>> getAllTrainerVideoAlbums();

    @GetMapping(path = "/getMyTrainerVideoAlbums")
    ResponseEntity<List<TrainerVideoAlbum>> getMyTrainerVideoAlbums();

    @PutMapping(path = "/disableClientReview/{id}")
    ResponseEntity<String> disableClientReview(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getMyClientReviews")
    ResponseEntity<List<ClientReview>> getMyClientReviews();

    @GetMapping(path = "/getAllClientReviews")
    ResponseEntity<List<ClientReview>> getAllClientReviews();

    @GetMapping(path = "/getActiveClientReviews/{id}")
    ResponseEntity<List<ClientReview>> getActiveClientReviews(@PathVariable Integer id);

    @PostMapping(path = "/addTrainerFeatureVideo")
    ResponseEntity<String> addTrainerFeatureVideo(@ModelAttribute FeatureVideoRequest featureVideoRequest) throws JsonProcessingException;

    @PutMapping(path = "/updateTrainerFeatureVideo")
    ResponseEntity<String> updateTrainerFeatureVideo(@ModelAttribute FeatureVideoRequest featureVideoRequest) throws JsonProcessingException;

    @DeleteMapping(path = "/deleteTrainerFeatureVideo/{id}")
    ResponseEntity<String> deleteTrainerFeatureVideo(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getAllTrainerFeatureVideos")
    ResponseEntity<List<TrainerFeatureVideo>> getAllTrainerFeatureVideos();

    @GetMapping(path = "/getMyTrainerFeatureVideos")
    ResponseEntity<List<TrainerFeatureVideo>> getMyTrainerFeatureVideos();

    @PutMapping(path = "/likeTrainerClientReview/{id}")
    ResponseEntity<String> likeClientReview(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getTrainerClientReviewLikes")
    ResponseEntity<List<ClientReviewLike>> getClientReviewLikes() throws JsonProcessingException;

    @GetMapping(path = "/getMyCenterTrainers")
    ResponseEntity<List<CenterTrainer>> getMyCenterTrainers() throws JsonProcessingException;
}
