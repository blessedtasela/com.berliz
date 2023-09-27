package com.berliz.serializers;

import com.berliz.models.Category;
import com.berliz.models.Center;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hibernate.proxy.HibernateProxy;

import java.io.IOException;

public class CenterSerializer extends JsonSerializer<Center> {

    @Override
    public void serialize(Center center, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (center instanceof HibernateProxy) {
            // Unwrap the proxy to the actual entity
            center = (Center) ((HibernateProxy) center).getHibernateLazyInitializer().getImplementation();
        }
        // Implement the serialization logic here
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", center.getId());
        jsonGenerator.writeStringField("name", center.getName());
        jsonGenerator.writeStringField("motto", center.getMotto());
        jsonGenerator.writeStringField("introduction", center.getIntroduction());
        jsonGenerator.writeStringField("address", center.getAddress());
        jsonGenerator.writeStringField("location", center.getLocation());
        jsonGenerator.writeStringField("photo", center.getPhoto());
        jsonGenerator.writeNumberField("likes", center.getLikes());

        // Serialize the partner using the PartnerObjectSerializer
        jsonGenerator.writeFieldName("partner");
        PartnerObjectSerializer partnerObjectSerializer = new PartnerObjectSerializer();
        partnerObjectSerializer.serialize(center.getPartner(), jsonGenerator, serializerProvider);

        // Serialize the categorySet using a loop
        jsonGenerator.writeArrayFieldStart("categorySet");
        for (Category category : center.getCategorySet()) {
            jsonGenerator.writeObject(category);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeStringField("date", center.getDate().toString());
        jsonGenerator.writeStringField("lastUpdate", center.getLastUpdate().toString());
        jsonGenerator.writeStringField("status", center.getStatus());
        jsonGenerator.writeEndObject();
    }
}
