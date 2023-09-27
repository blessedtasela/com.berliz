package com.berliz.serializers;

import com.berliz.models.Order;
import com.berliz.models.OrderDetails;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class OrderSerializer extends JsonSerializer<Order> {

    @Override
    public void serialize(Order order, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        // Implement the serialization logic here
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", order.getId());
        jsonGenerator.writeStringField("uuid", order.getUuid());

        // Write the "user" field name
        jsonGenerator.writeFieldName("user");

        // Serialize the product within OrderDetails using its custom serializer
        UserObjectSerializer orderUserSerializer = new UserObjectSerializer();
        orderUserSerializer.serialize(order.getUser(), jsonGenerator, serializerProvider);

        // Serialize the orderDetailsSet using a loop
        jsonGenerator.writeArrayFieldStart("orderDetailsSet");
        for (OrderDetails orderDetails : order.getOrderDetailsSet()) {
            jsonGenerator.writeObject(orderDetails);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeNumberField("totalAmount", order.getTotalAmount());
        jsonGenerator.writeStringField("paymentMethod", order.getPaymentMethod());
        jsonGenerator.writeStringField("address", order.getAddress());
        jsonGenerator.writeStringField("city", order.getCity());
        jsonGenerator.writeStringField("contact", order.getContact());
        jsonGenerator.writeStringField("country", order.getCountry());
        jsonGenerator.writeStringField("email", order.getEmail());
        jsonGenerator.writeStringField("name", order.getName());
        jsonGenerator.writeNumberField("postalCode", order.getPostalCode());
        jsonGenerator.writeStringField("state", order.getState());
        jsonGenerator.writeStringField("lastUpdate", order.getLastUpdate().toString());
        jsonGenerator.writeStringField("date", order.getDate().toString());
        jsonGenerator.writeStringField("status", order.getStatus());

        jsonGenerator.writeEndObject();
    }
}
