package com.shop.service;

import com.shop.entity.Item;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DiscountService { // 할인율메서드 8월16일
    public BigDecimal calculateFinalPrice(Item item) {
        BigDecimal discountRate = item.getDiscountrate(); // 필드 값을 직접 가져옴  할인율가져오기
        BigDecimal price = item.getPrice(); // 원래가격 가져오기

        if (discountRate != null && discountRate.compareTo(BigDecimal.ZERO) > 0) { // 할인율이 0보다크면 할인적용
            return price.subtract(price.multiply(discountRate));
        }
        return price; // 할인율없으면 원래가격반환
    }
}
