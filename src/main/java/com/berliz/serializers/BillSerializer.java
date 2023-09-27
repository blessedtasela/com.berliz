package com.berliz.serializers;

import com.berliz.models.Bill;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class BillSerializer extends JsonSerializer<Bill> {

    @Override
    public void serialize(Bill bill, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        // Implement the serialization logic here
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", bill.getId());
        jsonGenerator.writeStringField("uuid", bill.getUuid());

        // Write the "user" field name
        jsonGenerator.writeFieldName("order");

        // Serialize the product within OrderDetails using its custom serializer
        OrderObjectSerializer orderObjectSerializer = new OrderObjectSerializer();
        orderObjectSerializer.serialize(bill.getOrder(), jsonGenerator, serializerProvider);


        // Write the "user" field name
        jsonGenerator.writeFieldName("user");

        // Serialize the user using its custom serializer
        UserObjectSerializer userObjectSerializer = new UserObjectSerializer();
        userObjectSerializer.serialize(bill.getUser(), jsonGenerator, serializerProvider);

        jsonGenerator.writeStringField("date", String.valueOf(bill.getDate()));
        jsonGenerator.writeStringField("lastPrinted", String.valueOf(bill.getLastPrinted()));

        jsonGenerator.writeEndObject();
    }
}
