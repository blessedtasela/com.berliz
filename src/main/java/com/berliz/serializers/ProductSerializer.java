package com.berliz.serializers;

import com.berliz.models.Category;
import com.berliz.models.OrderDetails;
import com.berliz.models.Product;
import com.berliz.models.Tag;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hibernate.proxy.HibernateProxy;

import java.io.IOException;

public class ProductSerializer extends JsonSerializer<Product> {

    @Override
    public void serialize(Product product, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (product instanceof HibernateProxy) {
            // Unwrap the proxy to the actual entity
            product = (Product) ((HibernateProxy) product).getHibernateLazyInitializer().getImplementation();
        }
        // Implement the serialization logic here
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", product.getId());
        jsonGenerator.writeStringField("uuid", product.getUuid());
        jsonGenerator.writeStringField("name", product.getName());
        jsonGenerator.writeStringField("description", product.getDescription());
        jsonGenerator.writeStringField("sex", product.getSex());

        // Serialize the brand using the BrandObjectSerializer
        jsonGenerator.writeFieldName("brand");
        BrandObjectSerializer brandObjectSerializer = new BrandObjectSerializer();
        brandObjectSerializer.serialize(product.getBrand(), jsonGenerator, serializerProvider);

        // Serialize the store using the StoreObjectSerializer
        jsonGenerator.writeFieldName("store");
        StoreObjectSerializer storeObjectSerializer = new StoreObjectSerializer();
        storeObjectSerializer.serialize(product.getStore(), jsonGenerator, serializerProvider);

        jsonGenerator.writeNumberField("likes", product.getLikes());
        jsonGenerator.writeNumberField("price", product.getPrice());
        jsonGenerator.writeNumberField("quantity", product.getQuantity());
        jsonGenerator.writeStringField("photo", product.getPhoto());

        // Serialize the categorySet using a loop
        jsonGenerator.writeArrayFieldStart("categorySet");
        for (Category category : product.getCategorySet()) {
            jsonGenerator.writeObject(category);
        }
        jsonGenerator.writeEndArray();

        // Serialize the tagSet using a loop
        jsonGenerator.writeArrayFieldStart("tagSet");
        for (Tag tag : product.getTagSet()) {
            jsonGenerator.writeObject(tag);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeStringField("date", product.getDate().toString());
        jsonGenerator.writeStringField("lastUpdate", product.getLastUpdate().toString());
        jsonGenerator.writeStringField("status", product.getStatus());

        jsonGenerator.writeEndObject();
    }
}
