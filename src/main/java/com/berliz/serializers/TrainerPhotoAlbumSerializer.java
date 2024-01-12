package com.berliz.serializers;

import com.berliz.constants.BerlizConstants;
import com.berliz.models.Center;
import com.berliz.models.TrainerPhotoAlbum;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.io.FileUtils;
import org.hibernate.proxy.HibernateProxy;

import java.io.File;
import java.io.IOException;

public class TrainerPhotoAlbumSerializer extends JsonSerializer<TrainerPhotoAlbum> {

    @Override
    public void serialize(TrainerPhotoAlbum album, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (album instanceof HibernateProxy) {
            // Unwrap the proxy to the actual entity
            album = (TrainerPhotoAlbum) ((HibernateProxy) album).getHibernateLazyInitializer().getImplementation();
        }
        // Implement the serialization logic here
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", album.getId());
        jsonGenerator.writeObjectField("trainer", album.getTrainer());
        jsonGenerator.writeStringField("uuid", album.getUuid());
        jsonGenerator.writeStringField("comment", album.getComment());

        // Load photo content from the project folder
        byte[] photoContent = loadPhotoFromFolder(album.getPhoto());
        if (photoContent != null) {
            jsonGenerator.writeBinaryField("photo", photoContent);
        } else {
            jsonGenerator.writeNullField("photo");
        }

        jsonGenerator.writeStringField("date", album.getDate().toString());
        jsonGenerator.writeStringField("lastUpdate", album.getLastUpdate().toString());
        jsonGenerator.writeEndObject();
    }

    private byte[] loadPhotoFromFolder(String photoName) throws IOException {
        // Specify the folder path where photos are stored
        String photoFolderPath = BerlizConstants.TRAINER_PHOTO_ALBUM_LOCATION;

        // Create a File instance for the photo
        File photoFile = new File(photoFolderPath + photoName);

        // Read the photo content into a byte array
        return FileUtils.readFileToByteArray(photoFile);
    }
}
