package com.berliz.serializers;

import com.berliz.models.Photo;
import com.berliz.models.TrainerPhotoAlbum;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hibernate.proxy.HibernateProxy;

import java.io.IOException;
import java.util.List;

public class TrainerPhotoAlbumSerializer extends JsonSerializer<TrainerPhotoAlbum> {

    @Override
    public void serialize(TrainerPhotoAlbum album, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (album instanceof HibernateProxy) {
            album = (TrainerPhotoAlbum) ((HibernateProxy) album).getHibernateLazyInitializer().getImplementation();
        }

        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", album.getId());
        jsonGenerator.writeObjectField("trainer", album.getTrainer());
        jsonGenerator.writeStringField("comment", album.getComment());
        jsonGenerator.writeStringField("date", album.getDate().toString());
        jsonGenerator.writeStringField("lastUpdate", album.getLastUpdate().toString());

//        // Serialize the photo list
//        List<Photo> photos = album.get(); // Assuming getPhotos() returns List<Photo>
//        jsonGenerator.writeArrayFieldStart("photos");
//
//        for (Photo photo : photos) {
//            jsonGenerator.writeStartObject();
//            jsonGenerator.writeNumberField("id", photo.getId());
//            jsonGenerator.writeStringField("photoUrl", photo.getPhotoUrl());
//            jsonGenerator.writeStringField("name", photo.getName());
//            jsonGenerator.writeStringField("mimeType", photo.getMimeType());
//            jsonGenerator.writeNumberField("byteSize", photo.getByteSize());
//            jsonGenerator.writeStringField("caption", photo.getCaption() != null ? photo.getCaption() : "");
//            jsonGenerator.writeStringField("ownerType", photo.getOwnerType());
//            jsonGenerator.writeNumberField("ownerId", photo.getOwnerId());
//            jsonGenerator.writeStringField("date", photo.getDate() != null ? photo.getDate().toString() : "");
//            jsonGenerator.writeStringField("lastUpdate", photo.getLastUpdate() != null ? photo.getLastUpdate().toString() : "");
//            jsonGenerator.writeEndObject();
//        }

        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }
}
