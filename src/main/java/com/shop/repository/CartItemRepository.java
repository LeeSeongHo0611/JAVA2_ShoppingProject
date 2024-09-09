package com.shop.repository;

import com.shop.dto.CartDetailDto;
import com.shop.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    CartItem findByCartIdAndItemId(Long cartId, Long itemId);


    //조회할 항목 상품id  i.id 추가  8월27일
    // i.discountrate 추가 9월9일  Dto하고 쿼리문하고 순서 맞출것
    @Query("select new com.shop.dto.CartDetailDto(ci.id, i.id,  i.itemNm, i.price, ci.count, im.imgUrl, i.discountrate) " +
            "from CartItem ci, ItemImg im "+
            "join ci.item i " +
            "where ci.cart.id = :cartId " +
            "and im.item.id = ci.item.id " +
            "and im.repImgYn = 'Y' " +
            "order by ci.regTime desc")
    List<CartDetailDto> findCartDetailDtoList(Long cartId);

}
