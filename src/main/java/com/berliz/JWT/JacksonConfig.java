package com.berliz.JWT;

import com.berliz.models.*;
import com.berliz.serializers.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(User.class, new UserSerializer());
        module.addSerializer(Order.class, new OrderSerializer());
        module.addSerializer(Product.class, new ProductSerializer());
        module.addSerializer(OrderDetails.class, new OrderDetailsSerializer());
        module.addSerializer(Store.class, new StoreSerializer());
        module.addSerializer(Partner.class, new PartnerSerializer());
        module.addSerializer(Trainer.class, new TrainerSerializer());
        module.addSerializer(Center.class, new CenterSerializer());
        module.addSerializer(Driver.class, new DriverSerializer());
        module.addSerializer(Category.class, new CategorySerializer());
        module.addSerializer(TrainerPhotoAlbum.class, new TrainerPhotoAlbumSerializer());
        module.addSerializer(TrainerVideoAlbum.class, new TrainerVideoAlbumSerializer());
        module.addSerializer(CenterPhotoAlbum.class, new CenterPhotoAlbumSerializer());
        module.addSerializer(CenterVideoAlbum.class, new CenterVideoAlbumSerializer());
        module.addSerializer(TrainerClientReview.class, new TrainerClientReviewSerializer());
        module.addSerializer(TrainerFeatureVideo.class, new TrainerFeatureVideoSerializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
