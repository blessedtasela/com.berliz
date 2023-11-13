package com.berliz.serializers;

import com.berliz.models.Category;
import com.berliz.models.Product;
import com.berliz.models.Tag;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hibernate.proxy.HibernateProxy;

import java.io.IOException;

public class CategorySerializer extends JsonSerializer<Category> {

    @Override
    public void serialize(Category category, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (category instanceof HibernateProxy) {
            // Unwrap the proxy to the actual entity
            category = (Category) ((HibernateProxy) category).getHibernateLazyInitializer().getImplementation();
        }
        // Implement the serialization logic here
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", category.getId());
        jsonGenerator.writeStringField("name", category.getName());
        jsonGenerator.writeStringField("motto", category.getDescription());
        jsonGenerator.writeStringField("photo", category.getPhoto());
        jsonGenerator.writeNumberField("likes", category.getLikes());
        jsonGenerator.writeStringField("description", category.getDescription());

        // Serialize the tagSet using a loop
        jsonGenerator.writeArrayFieldStart("tagSet");
        for (Tag tag : category.getTagSet()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeNumberField("id", tag.getId());
            jsonGenerator.writeStringField("name", tag.getName());
            jsonGenerator.writeStringField("description", tag.getDescription());
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeStringField("date", category.getDate().toString());
        jsonGenerator.writeStringField("lastUpdate", category.getLastUpdate().toString());
        jsonGenerator.writeStringField("status", category.getStatus());
        jsonGenerator.writeEndObject();
    }
}