package com.berliz.repositories;

import com.berliz.models.Center;
import com.berliz.models.CenterPricing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CenterPricingRepo  extends JpaRepository<CenterPricing, Integer> {

    CenterPricing findByCenter(Center center);
}
