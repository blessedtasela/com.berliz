package com.berliz.repository;

import com.berliz.models.Center;
import com.berliz.models.CenterPricing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CenterPricingRepo  extends JpaRepository<CenterPricing, Integer> {

    CenterPricing findByCenter(Center center);
}
