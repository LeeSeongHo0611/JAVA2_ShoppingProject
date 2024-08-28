package com.shop.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CartDetailDto {
    private Long cartItemId; // 장바구니 상품 아이디
    private Long itemId; // 상품아이디 추가 8월27일
    private String itemNm; // 상품명
    private BigDecimal price; //
    private BigDecimal finalPrice; // 할인된 가격 설정 db에안들어감 생성자안만듬 서비스에서 가격계산되면 설정됨 8월27일
    private int count; // 수량
    private String imgUrl; // 상품이미지 경로
    public CartDetailDto(Long cartItemId, Long itemId, String itemNm,BigDecimal price, int count, String imgUrl){ //  itemId 추가 8월27일
        this.cartItemId = cartItemId;
        this.itemId = itemId; // itemId 추가 8월27일
        this.itemNm = itemNm;
        this.price = price;
        this.imgUrl = imgUrl;
        this.count = count;
    }
}