package com.berliz.serializers;

import com.berliz.models.Category;
import com.berliz.models.Store;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hibernate.proxy.HibernateProxy;

import java.io.IOException;

public class StoreSerializer extends JsonSerializer<Store> {

    @Override
    public void serialize(Store store, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (store instanceof HibernateProxy) {
            // Unwrap the proxy to the actual entity
            store = (Store) ((HibernateProxy) store).getHibernateLazyInitializer().getImplementation();
        }
        // Implement the serialization logic here
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", store.getId());
        jsonGenerator.writeStringField("name", store.getName());
        jsonGenerator.writeStringField("motto", store.getMotto());
        jsonGenerator.writeStringField("address", store.getAddress());
        jsonGenerator.writeStringField("introduction", store.getIntroduction());
        jsonGenerator.writeStringField("location", store.getLocation());
        jsonGenerator.writeStringField("photo", store.getPhoto());
        jsonGenerator.writeNumberField("likes", store.getLikes());

        // Serialize the partner using the OrderUserSerializer
        jsonGenerator.writeFieldName("partner");
        PartnerObjectSerializer partnerObjectSerializer = new PartnerObjectSerializer();
        partnerObjectSerializer.serialize(store.getPartner(), jsonGenerator, serializerProvider);

        // Serialize the categorySet using a loop
        jsonGenerator.writeArrayFieldStart("categorySet");
        for (Category category : store.getCategorySet()) {
            jsonGenerator.writeObject(category);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeStringField("date", store.getDate().toString());
        jsonGenerator.writeStringField("lastUpdate", store.getLastUpdate().toString());
        jsonGenerator.writeStringField("status", store.getStatus());
        jsonGenerator.writeEndObject();
    }
}
