package com.shop.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CartDetailDto {
    private Long cartItemId; // 장바구니 상품 아이디
    private Long itemId; // 상품아이디 추가 8월27일
    private String itemNm; // 상품명
    private int price; // // int로 다시 수정 9월3일
    private int finalPrice; // 할인된 가격 설정 db에안들어감 생성자안만듬 서비스에서 가격계산되면 설정됨 8월27일 // int로 다시 수정 9월3일
    private int count; // 수량
    private String imgUrl; // 상품이미지 경로
    private int discountrate; // 할인율  9월6일추가


    public CartDetailDto(Long cartItemId, Long itemId, String itemNm,int price, int count, String imgUrl, int discountrate){ //  itemId 추가 8월27일  생성자 discountrate 추가 9월9일
        // Dto하고 쿼리문하고 순서 맞출것
        this.cartItemId = cartItemId;
        this.itemId = itemId; // itemId 추가 8월27일
        this.itemNm = itemNm;
        this.price = price;
        this.imgUrl = imgUrl;
        this.count = count;
        this.discountrate = discountrate; // discountrate 추가 9월9일
    }
}