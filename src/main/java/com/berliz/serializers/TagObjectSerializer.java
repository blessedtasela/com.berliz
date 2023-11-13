package com.berliz.serializers;

import com.berliz.models.Product;
import com.berliz.models.Tag;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hibernate.proxy.HibernateProxy;

import java.io.IOException;

public class TagObjectSerializer extends JsonSerializer<Tag> {

    @Override
    public void serialize(Tag tag, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (tag instanceof HibernateProxy) {
            // Unwrap the proxy to the actual entity
            tag = (Tag) ((HibernateProxy) tag).getHibernateLazyInitializer().getImplementation();
        }
        // Implement the serialization logic here
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", tag.getId());
        jsonGenerator.writeStringField("name", tag.getName());
        jsonGenerator.writeStringField("description", tag.getDescription());
        // Add more fields as needed
        jsonGenerator.writeEndObject();
    }
}