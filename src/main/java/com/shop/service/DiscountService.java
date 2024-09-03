//package com.shop.service;
//
//import com.shop.entity.Item;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//
//@Service
//public class DiscountService { // 할인율메서드 8월19일    8월22일 현재 안쓰는중 엔티티에서 할인율계산중
//    public BigDecimal calculateFinalPrice(Item item) {
//        // 할인율이 null이면 0으로 처리
//        BigDecimal discountRate = item.getDiscountrate() != null ? item.getDiscountrate() : BigDecimal.ZERO;
//        BigDecimal price = item.getPrice(); // 원래 가격 가져오기
//
//        if (discountRate != null && discountRate.compareTo(BigDecimal.ZERO) > 0) { // 할인율이 0보다크면 할인적용
//            discountRate = discountRate.divide(BigDecimal.valueOf(100)); // 할인율 0~99 사이로 입력하기때문에  100으로 나눔
//            return price.subtract(price.multiply(discountRate));
//        }
//        return price; // 할인율없으면 원래가격반환
//    }
//}


package com.shop.service;

import com.shop.entity.Item;
import org.springframework.stereotype.Service;

import java.math.RoundingMode; // 반올림

@Service
public class DiscountService {

    // 할인율이 적용된 최종 가격 계산 메서드 8월22일
    public int calculateFinalPrice(Item item) {
        // 할인율을 가져오고, null이면 0을 할당 9월3일 변경
        Integer discountRateObj = item.getDiscountrate();
        int discountRate = discountRateObj != null ? discountRateObj : 0;
        int price = item.getPrice(); // 원래 가격 가져오기

        if (discountRate > 0) { // 할인율이 0보다 크면 할인 적용 int로 변경 맞게 표현식도변경 9월3일
            int discount = (price * discountRate) / 100; // 할인율 0~99 사이로 입력하기때문에  100으로 나눔, 소수점 이하 버림  int에 맞게 표현식도 변경 9월3일
            int finalPrice = price - discount; // 최종 가격 계산   int에 맞게 표현식도 변경 9월3일
            return Math.round(finalPrice / 10.0f) * 10; // 십의 자리에서 반올림 int에 맞게 표현식도 변경 9월3일
        }
        return Math.round(price / 10.0f) * 10; // 십의 자리에서 반올림한 원래 가격 반환  int에 맞게 표현식도 변경 9월3일
    }
}