package com.berliz.serializers;

import com.berliz.models.Category;
import com.berliz.models.Trainer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hibernate.proxy.HibernateProxy;

import java.io.IOException;
import java.util.Date;

public class TrainerSerializer extends JsonSerializer<Trainer> {

    @Override
    public void serialize(Trainer trainer, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (trainer instanceof HibernateProxy) {
            // Unwrap the proxy to the actual entity
            trainer = (Trainer) ((HibernateProxy) trainer).getHibernateLazyInitializer().getImplementation();
        }
        // Implement the serialization logic here
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", trainer.getId());
        jsonGenerator.writeStringField("name", trainer.getName());
        jsonGenerator.writeStringField("motto", trainer.getMotto());
        jsonGenerator.writeStringField("address", trainer.getAddress());
        jsonGenerator.writeStringField("experience", trainer.getExperience());
        jsonGenerator.writeBinaryField("photo", trainer.getPhoto());
        jsonGenerator.writeNumberField("likes", trainer.getLikes());

        // Serialize the partner using the PartnerObjectSerializer
        jsonGenerator.writeFieldName("partner");
        PartnerObjectSerializer partnerObjectSerializer = new PartnerObjectSerializer();
        partnerObjectSerializer.serialize(trainer.getPartner(), jsonGenerator, serializerProvider);

        // Serialize the categorySet using a loop
        jsonGenerator.writeArrayFieldStart("categorySet");
        for (Category category : trainer.getCategorySet()) {
            jsonGenerator.writeObject(category);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeStringField("date", trainer.getDate().toString());
        jsonGenerator.writeStringField("lastUpdate", trainer.getLastUpdate().toString());
        jsonGenerator.writeStringField("status", trainer.getStatus());
        jsonGenerator.writeEndObject();
    }
}
