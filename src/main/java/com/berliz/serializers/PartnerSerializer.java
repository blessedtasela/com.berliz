package com.berliz.serializers;

import com.berliz.models.Partner;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class PartnerSerializer extends JsonSerializer<Partner> {

    @Override
    public void serialize(Partner partner, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", partner.getId());

        // Write the "user" field name
        jsonGenerator.writeFieldName("user");

        // Serialize the product within OrderDetails using its custom serializer
        UserObjectSerializer orderUserSerializer = new UserObjectSerializer();
        orderUserSerializer.serialize(partner.getUser(), jsonGenerator, serializerProvider);

        jsonGenerator.writeStringField("role", partner.getRole());
        jsonGenerator.writeBinaryField("certificate", partner.getCertificate());
        jsonGenerator.writeStringField("motivation", partner.getMotivation());
        jsonGenerator.writeBinaryField("cv", partner.getCv());
        jsonGenerator.writeStringField("facebookUrl", partner.getFacebookUrl());
        jsonGenerator.writeStringField("instagramUrl", partner.getInstagramUrl());
        jsonGenerator.writeStringField("youtubeUrl", partner.getYoutubeUrl());
        jsonGenerator.writeStringField("date", String.valueOf(partner.getDate()));
        jsonGenerator.writeStringField("lastUpdate", String.valueOf(partner.getLastUpdate()));
        jsonGenerator.writeStringField("status", partner.getStatus());
        // Include more partner fields here

        jsonGenerator.writeEndObject();
    }
}
