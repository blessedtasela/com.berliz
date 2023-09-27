package com.berliz.serializers;

import com.berliz.models.OrderDetails;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class OrderDetailsSerializer extends JsonSerializer<OrderDetails> {

    @Override
    public void serialize(OrderDetails orderDetails, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        // Implement the serialization logic here
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", orderDetails.getId());

        // Write the "product" field name
        jsonGenerator.writeFieldName("product");

        // Serialize the product within OrderDetails using its custom serializer
        ProductObjectSerializer productSerializer = new ProductObjectSerializer();
        productSerializer.serialize(orderDetails.getProduct(), jsonGenerator, serializerProvider);

        jsonGenerator.writeNumberField("quantity", orderDetails.getQuantity());
        jsonGenerator.writeNumberField("total", orderDetails.getSubTotal());
        jsonGenerator.writeEndObject();
    }
}
