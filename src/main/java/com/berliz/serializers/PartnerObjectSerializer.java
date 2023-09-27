package com.berliz.serializers;

import com.berliz.models.Partner;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class PartnerObjectSerializer extends JsonSerializer<Partner> {

    @Override
    public void serialize(Partner partner, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", partner.getId());
        jsonGenerator.writeStringField("motivation", partner.getMotivation());

        // Write the "user" field name
        jsonGenerator.writeFieldName("user");

        // Serialize the product within OrderDetails using its custom serializer
        UserObjectSerializer userObjectSerializer = new UserObjectSerializer();
        userObjectSerializer.serialize(partner.getUser(), jsonGenerator, serializerProvider);

        // add more fields if needed

        jsonGenerator.writeEndObject();
    }
}
