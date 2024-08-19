package com.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class OrderItem extends BaseEntity{
    @Id
    @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id") // 외래키
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id") // 외래키
    private Order order;

    private BigDecimal orderPrice; // BigDecimal 변경 8월19일
    private int count;
    //private LocalDateTime regTime;
    //private LocalDateTime updateTime;

    //item(상품) -> OrderItem(주문 상품)
    public static OrderItem createOrderItem(Item item, int count){
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setCount(count);
        orderItem.setOrderPrice(item.getPrice());
        item.removeStock(count); //
        return orderItem;
    }
    public BigDecimal getTotalPrice(){
        return orderPrice.multiply(BigDecimal.valueOf(count)); // count BigDecimal로 바꿔서계산 8월19일
    }

    public void cancel(){
        this.getItem().addStock(count);
    }
}
