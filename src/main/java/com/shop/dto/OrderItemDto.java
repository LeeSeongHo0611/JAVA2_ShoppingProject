package com.shop.dto;

import com.shop.entity.OrderItem;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class OrderItemDto {
    private String itemNm;
    private int count;
    private int orderPrice; // int -> BigDecimal 변경 8월19일 // 다시int로변경 9월3일
    private String imgUrl;
    public OrderItemDto(OrderItem orderItem, String imgUrl){
        this.itemNm = orderItem.getItem().getItemNm();
        this.count = orderItem.getCount();
        this.orderPrice = orderItem.getOrderPrice();
        this.imgUrl = imgUrl;
    }
}
