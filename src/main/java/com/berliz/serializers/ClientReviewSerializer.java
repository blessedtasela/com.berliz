package com.berliz.serializers;

import com.berliz.constants.BerlizConstants;
import com.berliz.models.ClientReview;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.io.FileUtils;
import org.hibernate.proxy.HibernateProxy;

import java.io.File;
import java.io.IOException;

public class ClientReviewSerializer extends JsonSerializer<ClientReview> {

    @Override
    public void serialize(ClientReview clientReview, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (clientReview instanceof HibernateProxy) {
            // Unwrap the proxy to the actual entity
            clientReview = (ClientReview) ((HibernateProxy) clientReview).getHibernateLazyInitializer().getImplementation();
        }
        // Implement the serialization logic here
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", clientReview.getId());
        jsonGenerator.writeObjectField("trainer", clientReview.getTrainer());
        jsonGenerator.writeStringField("review", clientReview.getReview());
        jsonGenerator.writeNumberField("likes", clientReview.getLikes());

        // Load photo content from the project folder
        byte[] frontBeforePhoto = loadFileFromFolder(clientReview.getFrontBefore());
        byte[] frontAfterPhoto = loadFileFromFolder(clientReview.getFrontAfter());
        byte[] sideBeforePhoto = loadFileFromFolder(clientReview.getSideBefore());
        byte[] sideAfterPhoto = loadFileFromFolder(clientReview.getSideAfter());
        byte[] backBeforePhoto = loadFileFromFolder(clientReview.getBackBefore());
        byte[] backAfterPhoto = loadFileFromFolder(clientReview.getBackAfter());

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

        jsonGenerator.writeStringField("date", clientReview.getDate().toString());
        jsonGenerator.writeStringField("lastUpdate", clientReview.getLastUpdate().toString());
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


