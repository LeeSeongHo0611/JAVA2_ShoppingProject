package com.shop.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class MainItemDto {
    private Long id;
    private String itemNm;
    private String itemDetail;
    private String imgUrl;
    private int price; // int -> BigDecimal 변경 8월19일 // int로 다시 수정 9월3일
    private int discountrate; // 8월20일 추가 // int로 다시 수정 9월3일
    private int stockNumber;
    private int finalPrice; // 최종 가격 필드 추가 8월20일 // int로 다시 수정 9월3일

    @QueryProjection //Querydsl 결과 조회 시 MainItemDto 객체로 바로 오도록  활용
    public MainItemDto(Long id, String itemNm, String itemDetail, String imgUrl, int price, int discountrate, int stockNumber, int finalPrice){ // int -> BigDecimal 변경 8월19일
        this.id = id;
        this.itemNm = itemNm;
        this.itemDetail = itemDetail;
        this.imgUrl = imgUrl;
        this.price = price;
        this.discountrate = discountrate;
        this.stockNumber = stockNumber;
        this.finalPrice = finalPrice;

        // 로그 추가 8월21일
        System.out.println("MainItemDto created - id: " + id + ", itemNm: " + itemNm + ", finalPrice: " + finalPrice);

    }
}
