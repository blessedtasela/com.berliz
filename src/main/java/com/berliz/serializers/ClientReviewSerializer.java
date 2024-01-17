package com.berliz.serializers;

import com.berliz.constants.BerlizConstants;
import com.berliz.models.TrainerReview;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.io.FileUtils;
import org.hibernate.proxy.HibernateProxy;

import java.io.File;
import java.io.IOException;

public class ClientReviewSerializer extends JsonSerializer<TrainerReview> {

    @Override
    public void serialize(TrainerReview trainerReview, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (trainerReview instanceof HibernateProxy) {
            // Unwrap the proxy to the actual entity
            trainerReview = (TrainerReview) ((HibernateProxy) trainerReview).getHibernateLazyInitializer().getImplementation();
        }
        // Implement the serialization logic here
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", trainerReview.getId());
        jsonGenerator.writeObjectField("trainer", trainerReview.getTrainer());
        jsonGenerator.writeStringField("review", trainerReview.getReview());
        jsonGenerator.writeNumberField("likes", trainerReview.getLikes());

        // Load photo content from the project folder
        byte[] frontBeforePhoto = loadFileFromFolder(trainerReview.getFrontBefore());
        byte[] frontAfterPhoto = loadFileFromFolder(trainerReview.getFrontAfter());
        byte[] sideBeforePhoto = loadFileFromFolder(trainerReview.getSideBefore());
        byte[] sideAfterPhoto = loadFileFromFolder(trainerReview.getSideAfter());
        byte[] backBeforePhoto = loadFileFromFolder(trainerReview.getBackBefore());
        byte[] backAfterPhoto = loadFileFromFolder(trainerReview.getBackAfter());

        // Check if either the photos are not null
        if (containsNonNullPhoto(frontBeforePhoto, frontAfterPhoto, backBeforePhoto, backAfterPhoto, sideBeforePhoto, sideAfterPhoto)) {
            jsonGenerator.writeBinaryField("frontBefore", frontBeforePhoto);
            jsonGenerator.writeBinaryField("frontAfter", frontAfterPhoto);
            jsonGenerator.writeBinaryField("sideBefore", sideBeforePhoto);
            jsonGenerator.writeBinaryField("sideAfter", sideAfterPhoto);
            jsonGenerator.writeBinaryField("backBefore", backBeforePhoto);
            jsonGenerator.writeBinaryField("backAfter", backAfterPhoto);
        } else {
            writeNullFields(jsonGenerator, "frontBefore", "frontAfter", "sideBefore", "sideAfter", "backBefore", "backAfter");
        }

        jsonGenerator.writeStringField("date", trainerReview.getDate().toString());
        jsonGenerator.writeStringField("lastUpdate", trainerReview.getLastUpdate().toString());
        jsonGenerator.writeEndObject();
    }

    private void writeNullFields(JsonGenerator jsonGenerator, String... fieldNames) throws IOException {
        for (String fieldName : fieldNames) {
            jsonGenerator.writeNullField(fieldName);
        }
    }

    private boolean containsNonNullPhoto(byte[]... photos) {
        for (byte[] photo : photos) {
            if (photo != null) {
                return true;
            }
        }
        return false;
    }

    private byte[] loadFileFromFolder(String fileName) throws IOException {
        String fileFolderPath = BerlizConstants.TRAINER_CLIENT_REVIEW;
        File file = new File(fileFolderPath + fileName);
        return FileUtils.readFileToByteArray(file);
    }
}


