package com.shop.entity;

import com.shop.constant.ItemSellStatus;
import com.shop.dto.ItemFormDto;
import com.shop.exception.OutOfStockException;
import com.shop.service.DiscountService;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode; // 반올림
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "item")
@Getter
@Setter
@ToString
public class Item extends BaseEntity{
    @Id
    @Column(name = "item_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id; // 상품코드

    @Column(nullable = false,length = 50)
    private String itemNm; // 상품명

    @Column(name = "price", nullable = false)
    private BigDecimal price; // 가격 BigDecimal 8월19일변경

    private BigDecimal discountrate; // 할인율

    @Column(nullable = false)
    private int stockNumber; // 수량

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String itemDetail; // 상품 상세정보

    @Enumerated(EnumType.STRING)
    private ItemSellStatus itemSellStatus; // 상품판매 상태

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name="member_item",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id")
    )
    private List<Member> member;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItemImg> images = new ArrayList<>();

    // 상품 정보 업데이트
    public void updateItem(ItemFormDto itemFormDto){
        this.itemNm = itemFormDto.getItemNm();
        this.price = itemFormDto.getPrice();
        this.discountrate = itemFormDto.getDiscountrate(); // 할인율 추가 8월19일
        this.stockNumber = itemFormDto.getStockNumber();
        this.itemDetail = itemFormDto.getItemDetail();
        this.itemSellStatus = itemFormDto.getItemSellStatus();
    }

    // 재고 감소
    public void removeStock(int stockNumber){
        int restStock = this.stockNumber - stockNumber;
        if(restStock < 0){
            throw new OutOfStockException("상품의 재고가 부족합니다.(현재 재고 수량: "+this.stockNumber+")");
        }
        this.stockNumber = restStock;
    }

    // 재고 증가
    public void addStock(int stockNumber){
        this.stockNumber += stockNumber;
    }

//    // 할인율이 적용된 최종 가격 십의자리까지만 계산 8월22일
//    public BigDecimal getFinalPrice() {
//        if (this.discountrate != null && this.discountrate.compareTo(BigDecimal.ZERO) > 0) {
//            BigDecimal discount = this.price.multiply(this.discountrate).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
//            BigDecimal finalPrice = this.price.subtract(discount);
//
//            // 10원 단위에서 반올림 처리
//            return finalPrice.setScale(-1, RoundingMode.HALF_UP);
//        }
//        return this.price.setScale(-1, RoundingMode.HALF_UP);
//    }
//
//    // 가격을 문자열로 반환하여 E 표기법 문제를 해결 8월22일
//    public String getFormattedFinalPrice() {
//        return getFinalPrice().toPlainString() + "원";
//    }

    @Transient
    private BigDecimal finalPrice; // 최종 가격, DB에 저장하지 않음 8월22일

    // DiscountService를 사용하여 최종 가격 계산 8월22일
    public void calculateFinalPrice(DiscountService discountService) {
        this.finalPrice = discountService.calculateFinalPrice(this);
    }

    // 최종 가격을 문자열로 포맷하여 반환 8월22일
    public String getFormattedFinalPrice() {
        return finalPrice.toPlainString() + "원";
    }

}

