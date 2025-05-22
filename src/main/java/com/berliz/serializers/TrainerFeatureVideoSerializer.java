package com.berliz.serializers;

import com.berliz.constants.BerlizConstants;
import com.berliz.models.TrainerFeatureVideo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.io.FileUtils;
import org.hibernate.proxy.HibernateProxy;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class TrainerFeatureVideoSerializer extends JsonSerializer<TrainerFeatureVideo> {

    @Override
    public void serialize(TrainerFeatureVideo featureVideo, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (featureVideo instanceof HibernateProxy) {
            // Unwrap the proxy to the actual entity
            featureVideo = (TrainerFeatureVideo) ((HibernateProxy) featureVideo).getHibernateLazyInitializer().getImplementation();
        }
        // Implement the serialization logic here
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", featureVideo.getId());
        jsonGenerator.writeObjectField("trainer", featureVideo.getTrainer());
        jsonGenerator.writeStringField("motivation", featureVideo.getMotivation());

        // Load photo content from the project folder
        jsonGenerator.writeStringField("video", generateVideoUrl(featureVideo.getVideo()));

        jsonGenerator.writeStringField("date", featureVideo.getDate().toString());
        jsonGenerator.writeStringField("lastUpdate", featureVideo.getLastUpdate().toString());
        jsonGenerator.writeEndObject();
    }

    private byte[] loadVideoFromFolder(String videoName) throws IOException {
        // Specify the folder path where photos are stored
        String videoFolderPath = BerlizConstants.TRAINER_FEATURE_VIDEO_PATH;

        // Create a File instance for the photo
        File video = new File(videoFolderPath + videoName);

        // Read the photo content into a byte array
        return FileUtils.readFileToByteArray(video);
    }

    private String generateVideoUrl(String videoName) {
        // Base URL for your Heroku app
        return BerlizConstants.BERLIZ_SERVER_URL
                + BerlizConstants.TRAINER_FEATURE_VIDEO_PATH
                + videoName ;
//                + "?t=" + System.currentTimeMillis();
    }
}

