package com.berliz.serializers;

import com.berliz.constants.BerlizConstants;
import com.berliz.models.CenterVideoAlbum;
import com.berliz.models.TrainerVideoAlbum;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.io.FileUtils;
import org.hibernate.proxy.HibernateProxy;

import java.io.File;
import java.io.IOException;

public class CenterVideoAlbumSerializer  extends JsonSerializer<CenterVideoAlbum> {

    @Override
    public void serialize(CenterVideoAlbum album, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (album instanceof HibernateProxy) {
            // Unwrap the proxy to the actual entity
            album = (CenterVideoAlbum) ((HibernateProxy) album).getHibernateLazyInitializer().getImplementation();
        }
        // Implement the serialization logic here
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", album.getId());
        jsonGenerator.writeObjectField("center", album.getCenter());
        jsonGenerator.writeStringField("uuid", album.getUuid());
        jsonGenerator.writeStringField("comment", album.getComment());

        // Load photo content from the project folder
        byte[] videoContent = loadPhotoFromFolder(album.getVideo());
        if (videoContent != null) {
            jsonGenerator.writeBinaryField("video", videoContent);
        } else {
            jsonGenerator.writeNullField("video");
        }

        jsonGenerator.writeStringField("date", album.getDate().toString());
        jsonGenerator.writeStringField("lastUpdate", album.getLastUpdate().toString());
        jsonGenerator.writeEndObject();
    }

    private byte[] loadPhotoFromFolder(String videoName) throws IOException {
        // Specify the folder path where photos are stored
        String videoFolderPath = BerlizConstants.CENTER_VIDEO_ALBUM_LOCATION;

        // Create a File instance for the photo
        File videoFile = new File(videoFolderPath + videoName);

        // Read the photo content into a byte array
        return FileUtils.readFileToByteArray(videoFile);
    }
}


