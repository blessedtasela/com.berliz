package com.berliz.serializers;

import com.berliz.models.Store;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hibernate.proxy.HibernateProxy;

import java.io.IOException;

public class StoreObjectSerializer extends JsonSerializer<Store> {
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
        // Add more fields as needed
        jsonGenerator.writeEndObject();
    }
}
