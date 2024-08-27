package com.shop.entity;

import com.shop.service.DiscountService;
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
    public static OrderItem createOrderItem(Item item, int count, BigDecimal finalPrice){ //매개변수 finalPrice 추가 8월26일
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setCount(count);

        //orderItem.setOrderPrice(item.getPrice());


        orderItem.setOrderPrice(finalPrice);  // 할인된 가격을 설정 8월26일

        item.removeStock(count); // 재고 차감
        return orderItem;
    }
    public BigDecimal getTotalPrice(){ // 주문아이템의 총가격 계산
        return orderPrice.multiply(BigDecimal.valueOf(count)); // count BigDecimal로 바꿔서계산 8월19일
    }

    public void cancel(){
        this.getItem().addStock(count);
    }
}
