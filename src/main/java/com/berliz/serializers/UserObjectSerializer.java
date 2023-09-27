package com.berliz.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.berliz.models.User;
import org.hibernate.proxy.HibernateProxy;

import java.io.IOException;

public class UserObjectSerializer extends JsonSerializer<User> {

    @Override
    public void serialize(User user, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (user instanceof HibernateProxy) {
            // Unwrap the proxy to the actual entity
            user = (User) ((HibernateProxy) user).getHibernateLazyInitializer().getImplementation();
        }
        // Implement the serialization logic here
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", user.getId());
        jsonGenerator.writeStringField("email", user.getEmail());
        jsonGenerator.writeStringField("role", user.getRole());
        // Add more fields as needed
        jsonGenerator.writeEndObject();
    }
}
