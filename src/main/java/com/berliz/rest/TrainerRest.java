package com.berliz.rest;

import com.berliz.DTO.*;
import com.berliz.models.*;
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

    @GetMapping(path = "/getTrainerPricing")
    ResponseEntity<List<TrainerPricing>> getTrainerPricing();

    @DeleteMapping(path = "/deleteTrainerPricing/{id}")
    ResponseEntity<String> deleteTrainerPricing(@PathVariable Integer id) throws JsonProcessingException;

    @PutMapping(path = "/addTrainerPhotoAlbum")
    ResponseEntity<String> addTrainerPhotoAlbum(@ModelAttribute PhotoAlbum photoAlbum) throws JsonProcessingException;

    @PutMapping(path = "/updateTrainerPhotoAlbum")
    ResponseEntity<String> updateTrainerPhotoAlbum(@ModelAttribute PhotoAlbum photoAlbum) throws JsonProcessingException;

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
    ResponseEntity<String> addTrainerIntroduction(@ModelAttribute Introduction introduction) throws JsonProcessingException;

    @PutMapping(path = "/updateTrainerIntroduction")
    ResponseEntity<String> updateTrainerIntroduction(@ModelAttribute Introduction introduction) throws JsonProcessingException;

    @DeleteMapping(path = "/deleteTrainerIntroduction/{id}")
    ResponseEntity<String> deleteTrainerIntroduction(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getAllTrainerIntroductions")
    ResponseEntity<List<TrainerIntroduction>> getAllTrainerIntroductions();

    @GetMapping(path = "/getMyTrainerIntroduction")
    ResponseEntity<TrainerIntroduction> getMyTrainerIntroduction();

    @PostMapping(path = "/addTrainerVideoAlbum")
    ResponseEntity<String> addTrainerVideoAlbum(@ModelAttribute VideoAlbum videoAlbum) throws JsonProcessingException;

    @PutMapping(path = "/updateTrainerVideoAlbum")
    ResponseEntity<String> updateTrainerVideoAlbum(@ModelAttribute VideoAlbum videoAlbum) throws JsonProcessingException;

    @DeleteMapping(path = "/deleteTrainerVideoAlbum/{id}")
    ResponseEntity<String> deleteTrainerVideoAlbum(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getAllTrainerVideoAlbums")
    ResponseEntity<List<TrainerVideoAlbum>> getAllTrainerVideoAlbums();

    @GetMapping(path = "/getMyTrainerVideoAlbums")
    ResponseEntity<List<TrainerVideoAlbum>> getMyTrainerVideoAlbums();

    @PostMapping(path = "/addTrainerClientReview")
    ResponseEntity<String> addTrainerClientReview(@ModelAttribute ClientReview clientReview) throws JsonProcessingException;

    @PutMapping(path = "/updateTrainerClientReview")
    ResponseEntity<String> updateTrainerClientReview(@ModelAttribute ClientReview clientReview) throws JsonProcessingException;

    @DeleteMapping(path = "/deleteTrainerClientReview/{id}")
    ResponseEntity<String> deleteTrainerClientReview(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getAllTrainerClientReviews")
    ResponseEntity<List<TrainerClientReview>> getAllTrainerClientReviews();

    @GetMapping(path = "/getActiveTrainerClientReviews")
    ResponseEntity<List<TrainerClientReview>> getActiveTrainerClientReviews();

    @GetMapping(path = "/getMyTrainerClientReviews")
    ResponseEntity<List<TrainerClientReview>> getMyTrainerClientReviews();

    @PostMapping(path = "/addTrainerFeatureVideo")
    ResponseEntity<String> addTrainerFeatureVideo(@ModelAttribute FeatureVideo featureVideo) throws JsonProcessingException;

    @PutMapping(path = "/updateTrainerFeatureVideo")
    ResponseEntity<String> updateTrainerFeatureVideo(@ModelAttribute FeatureVideo featureVideo) throws JsonProcessingException;

    @DeleteMapping(path = "/deleteTrainerFeatureVideo/{id}")
    ResponseEntity<String> deleteTrainerFeatureVideo(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getAllTrainerFeatureVideos")
    ResponseEntity<List<TrainerFeatureVideo>> getAllTrainerFeatureVideos();

    @GetMapping(path = "/getMyTrainerFeatureVideos")
    ResponseEntity<List<TrainerFeatureVideo>> getMyTrainerFeatureVideos();

}
