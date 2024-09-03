package com.shop.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.constant.ItemSellStatus;
import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
import com.shop.dto.QMainItemDto;
import com.shop.entity.*;
import com.shop.service.DiscountService;
import jakarta.persistence.EntityManager;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.shop.entity.QItem.item;

@Log

public class ItemRepositoryCustomImpl implements ItemRepositoryCustom{


    private JPAQueryFactory queryFactory; // 동적쿼리 사용하기 위해 JPAQueryFactory 변수 선언
    private final DiscountService discountService; // DiscountService 변수 추가 8월21일
    
    // 생성자
    public ItemRepositoryCustomImpl(EntityManager em, DiscountService discountService){
        log.info("ItemRepositoryCustomImpl");
        this.queryFactory = new JPAQueryFactory(em); // JPAQueryFactory 실질적인 객체가 만들어 집니다.
        this.discountService = discountService; // DiscountService 주입 8월21일
    }

    private BooleanExpression searchSellStatusEq(ItemSellStatus searchSellStatus){
        return searchSellStatus == null ?
                null : item.itemSellStatus.eq(searchSellStatus);
        //ItemSellStatus null이면 null 리턴 null 아니면 SELL, SOLD 둘중 하나 리턴
    }

    private  BooleanExpression regDtsAfter(String searchDateType){ // all, 1d, 1w, 1m 6m
        LocalDateTime dateTime = LocalDateTime.now(); // 현재시간을 추출해서 변수에 대입

        if(StringUtils.equals("all",searchDateType) || searchDateType == null){
            return null;
        }else if(StringUtils.equals("1d",searchDateType)){
            dateTime = dateTime.minusDays(1);
        }else if(StringUtils.equals("1w",searchDateType)){
            dateTime = dateTime.minusWeeks(1);
        }else if(StringUtils.equals("1m",searchDateType)){
            dateTime = dateTime.minusMonths(1);
        }else if(StringUtils.equals("6m",searchDateType)){
            dateTime = dateTime.minusMonths(6);
        }
        return item.regTime.after(dateTime);
        //dateTime을 시간에 맞게 세팅 후 시간에 맞는 등록된 상품이 조회하도록 조건값 반환
    }

    private BooleanExpression searchByLike(String searchBy, String searchQuery){
        if(StringUtils.equals("itemNm",searchBy)){ // 상품명
            return item.itemNm.like("%"+searchQuery+"%");
        }else if(StringUtils.equals("createdBy",searchBy)){ // 작성자
            return item.createdBy.like("%"+searchQuery+"%");
        }
        return null;
    }

    @Override
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable){
        QueryResults<Item> results = queryFactory.selectFrom(item).
                where(regDtsAfter(itemSearchDto.getSearchDateType()),
                        searchSellStatusEq(itemSearchDto.getSearchSellStatus()),
                        searchByLike(itemSearchDto.getSearchBy(),itemSearchDto.getSearchQuery()))
                .orderBy(item.id.desc())
                .offset(pageable.getOffset()).limit(pageable.getPageSize()).fetchResults();
        List<Item> content = results.getResults();
        long total = results.getTotal();
        return new PageImpl<>(content,pageable,total);
    }
    private BooleanExpression itemNmLike(String searchQuery){
        return StringUtils.isEmpty(searchQuery) ? null : item.itemNm.like("%"+searchQuery+"%");
    }




        @Override
        public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable){ // 8월21일 수정
            QItem item = QItem.item;
            QItemImg itemImg = QItemImg.itemImg;

            // 계산된 finalPrice를 로그로 출력 8월21일
            log.info("Starting getMainItemPage" );

            //select i.id,id.itemNm,i.itemDetail,im.itemImg, i.price from item i, itemimg im join i.id = im.itemid
            //where im.repImgYn = "Y" and i.itemNm like %searchQuery% order by i.id desc
            //QMainItemDto @QueryProjection을 하용하면 DTO로 바로 조회 가능
            QueryResults<MainItemDto> results = queryFactory.select(new QMainItemDto(item.id, item.itemNm,
                            item.itemDetail,itemImg.imgUrl,item.price,
                            item.discountrate, // 할인율 추가 8월20일
                            item.stockNumber,
                            Expressions.constant(0) // finalPrice의 초기값을 0으로설정 8월22일
                            ))
                    // join 내부조인 .repImgYn.eq("Y") 대표이미지만 가져온다.
                    .from(itemImg).join(itemImg.item, item).where(itemImg.repImgYn.eq("Y"))
                    .where(itemNmLike(itemSearchDto.getSearchQuery()))
                    .orderBy(item.id.desc()).offset(pageable.getOffset()).limit(pageable.getPageSize()).fetchResults();

            List<MainItemDto> content = results.getResults();

            // 각 아이템에 대해 최종 가격을 계산하고 설정 8월21일    8월22일 수정
            for (MainItemDto dto : content) {

                // 데이터베이스에서 가져온 각 아이템에 대해 최종 가격을 설정 8월22일
                Item itemEntity = new Item();
                itemEntity.setPrice(dto.getPrice());
                itemEntity.setDiscountrate(dto.getDiscountrate());
                log.info("Item DTO 생성 전: " + dto); // 로그찍어보기 8월23일
                
                int finalPrice = discountService.calculateFinalPrice(itemEntity);// DiscountService를 사용하여 최종 가격 계산 8월26일 int로 다시 변경 9월3일
                
                dto.setFinalPrice(finalPrice);// 계산된 최종 가격을 DTO에 설정 8월26일
                log.info("Item DTO 생성 후, 최종 가격 설정: ID=" + dto.getId() + ", 이름=" + dto.getItemNm() + ", 최종 가격=" + dto.getFinalPrice());
                // 로그찍어보기 8월23일

            }

            long total = results.getTotal();
            return new PageImpl<>(content, pageable,total);
        }

    @Override
    public List<Item> findTopItemsByOrderCount(int limit) {
        QItem item = QItem.item;
        QOrderItem orderItem = QOrderItem.orderItem;

        return queryFactory
                .select(item)
                .from(item)
                .join(orderItem).on(item.id.eq(orderItem.item.id))
                .groupBy(item)
                .orderBy(orderItem.count.sum().desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<ItemImg> findImagesByItemIds(List<Long> itemIds) {
        QItemImg itemImg = QItemImg.itemImg;

        return queryFactory
                .select(itemImg)
                .from(itemImg)
                .where(itemImg.item.id.in(itemIds)) // 아이템 ID로 필터링
                .fetch();
    }
}