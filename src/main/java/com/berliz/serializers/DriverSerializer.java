package com.berliz.serializers;

import com.berliz.models.Category;
import com.berliz.models.Driver;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hibernate.proxy.HibernateProxy;

import java.io.IOException;

public class DriverSerializer extends JsonSerializer<Driver> {

    @Override
    public void serialize(Driver driver, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (driver instanceof HibernateProxy) {
            // Unwrap the proxy to the actual entity
            driver = (Driver) ((HibernateProxy) driver).getHibernateLazyInitializer().getImplementation();
        }
        // Implement the serialization logic here
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", driver.getId());
        jsonGenerator.writeStringField("name", driver.getName());
        jsonGenerator.writeStringField("vehicleType", driver.getVehicleType());
        jsonGenerator.writeStringField("vehicleModel", driver.getVehicleModel());
        jsonGenerator.writeStringField("licensePlate", driver.getLicensePlate());
        jsonGenerator.writeStringField("address", driver.getAddress());
        jsonGenerator.writeStringField("introduction", driver.getIntroduction());
        jsonGenerator.writeStringField("location", driver.getLocation());
        jsonGenerator.writeNumberField("likes", driver.getLikes());

        // Serialize the partner using the PartnerObjectSerializer
        jsonGenerator.writeFieldName("partner");
        PartnerObjectSerializer partnerObjectSerializer = new PartnerObjectSerializer();
        partnerObjectSerializer.serialize(driver.getPartner(), jsonGenerator, serializerProvider);

        jsonGenerator.writeStringField("date", driver.getDate().toString());
        jsonGenerator.writeStringField("lastUpdate", driver.getLastUpdate().toString());
        jsonGenerator.writeStringField("status", driver.getStatus());

        jsonGenerator.writeEndObject();
    }
}
