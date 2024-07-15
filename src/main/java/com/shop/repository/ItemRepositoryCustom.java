package com.shop.repository;

import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
import com.shop.entity.Item;
import com.shop.entity.ItemImg;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ItemRepositoryCustom {
    Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable);
    Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable);
    List<Item> findTopItemsByOrderCount(int limit);
    List<ItemImg> findImagesByItemIds(List<Long> itemIds); // 아이템 ID로 이미지 가져오는 메소드 추가
}
