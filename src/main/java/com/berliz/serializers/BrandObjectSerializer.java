package com.berliz.serializers;

import com.berliz.models.Brand;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hibernate.proxy.HibernateProxy;

import java.io.IOException;

public class BrandObjectSerializer extends JsonSerializer<Brand> {
    @Override
    public void serialize(Brand brand, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (brand instanceof HibernateProxy) {
            // Unwrap the proxy to the actual entity
            brand = (Brand) ((HibernateProxy) brand).getHibernateLazyInitializer().getImplementation();
        }
        // Implement the serialization logic here
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", brand.getId());
        jsonGenerator.writeStringField("name", brand.getName());
        // Add more fields as needed
        jsonGenerator.writeEndObject();
    }
}
