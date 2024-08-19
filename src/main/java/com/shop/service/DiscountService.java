package com.shop.service;

import com.shop.entity.Item;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DiscountService { // 할인율메서드 8월19일
    public BigDecimal calculateFinalPrice(Item item) {
        // 할인율이 null이면 0으로 처리
        BigDecimal discountRate = item.getDiscountrate() != null ? item.getDiscountrate() : BigDecimal.ZERO;
        BigDecimal price = item.getPrice(); // 원래 가격 가져오기

        if (discountRate != null && discountRate.compareTo(BigDecimal.ZERO) > 0) { // 할인율이 0보다크면 할인적용
            discountRate = discountRate.divide(BigDecimal.valueOf(100)); // 할인율 0~99 사이로 입력하기때문에  100으로 나눔
            return price.subtract(price.multiply(discountRate));
        }
        return price; // 할인율없으면 원래가격반환
    }
}
