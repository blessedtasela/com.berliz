package com.berliz.serializers;

import com.berliz.models.*;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class UserSerializer extends JsonSerializer<User> {

    @Override
    public void serialize(User user, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", user.getId());
        jsonGenerator.writeStringField("firstname", user.getFirstname());
        jsonGenerator.writeStringField("lastname", user.getLastname());
        jsonGenerator.writeStringField("phone", user.getPhone());
        jsonGenerator.writeStringField("dob", String.valueOf(user.getDob()));
        jsonGenerator.writeStringField("gender", user.getGender());
        jsonGenerator.writeStringField("country", user.getCountry());
        jsonGenerator.writeStringField("state", user.getState());
        jsonGenerator.writeStringField("city", user.getCity());
        jsonGenerator.writeNumberField("postalCode", user.getPostalCode());
        jsonGenerator.writeStringField("address", user.getAddress());
        jsonGenerator.writeStringField("email", user.getEmail());
        jsonGenerator.writeStringField("role", user.getRole());
        jsonGenerator.writeStringField("bio", user.getBio());
        jsonGenerator.writeBinaryField("profilePhoto", user.getProfilePhoto());

        // Serialize the category like using a loop
        jsonGenerator.writeArrayFieldStart("likedCategoriesSet");
        for (Category category : user.getLikedCategoriesSet()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeNumberField("id", category.getId());
            jsonGenerator.writeStringField("name", category.getName());
            jsonGenerator.writeStringField("description", category.getDescription());
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeStringField("status", user.getStatus());
        jsonGenerator.writeStringField("date", String.valueOf(user.getDate()));
        jsonGenerator.writeStringField("lastUpdate", String.valueOf(user.getLastUpdate()));

        // Add more fields as needed
        jsonGenerator.writeEndObject();
    }
}
