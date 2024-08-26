package com.shop.service;

import com.shop.dto.ItemFormDto;
import com.shop.dto.ItemImgDto;
import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
import com.shop.entity.Item;
import com.shop.entity.ItemImg;
import com.shop.repository.ItemImgRepository;
import com.shop.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Log
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemImgService itemImgService;
    private final ItemImgRepository itemImgRepository;
    private final DiscountService discountService; // DiscountService 주입 8월26일

    
    //8월20일 getItemById 추가
    public Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id " + id));
    }

    // ID로 Item을 찾는 메서드 추가 8월22일
    public Item findItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이템이 존재하지 않습니다. ID: " + id));
    }


    public Long saveItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList)
            throws Exception{
        //상품등록
        log.info("====================start:saveItem======================");
        Item item = itemFormDto.createItem();
        itemRepository.save(item);
        //이미지 등록
        for(int i =0;i<itemImgFileList.size();i++){
            ItemImg itemImg = new ItemImg();
            itemImg.setItem(item);
            if(i==0)
                itemImg.setRepImgYn("Y");
            else
                itemImg.setRepImgYn("N");
            itemImgService.saveItemImg(itemImg,itemImgFileList.get(i));
            System.out.println("breakPoint");

        }
        log.info("====================END:saveItem======================");
        return item.getId();
    }

    @Transactional(readOnly = true)
    public ItemFormDto getItemDtl(Long itemId){ // 8월26일 수정
        log.info("====================start:getItemDtl======================");
        //Entity
        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(itemId);
        //DB에서 데이터를 가지고 옵니다.
        //DTO
        List<ItemImgDto> itemImgDtoList = new ArrayList<>(); //왜 DTO 만들었나요??

        for(ItemImg itemimg : itemImgList){
            // Entity -> DTO
            ItemImgDto itemImgDto = ItemImgDto.of(itemimg);
            itemImgDtoList.add(itemImgDto);
        }

        Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
        // Item -> ItemFormDto modelMapper
        ItemFormDto itemFormDto = ItemFormDto.of(item);
        itemFormDto.setItemImgDtoList(itemImgDtoList);

        //finalPrice 계산 추가 8월26일
        BigDecimal finalPrice = discountService.calculateFinalPrice(item);
        itemFormDto.setFinalPrice(finalPrice); // 계산된 finalPrice를 DTO에 설정 8월26일

        log.info("====================END:getItemDtl======================");
        return itemFormDto;
    }

    public Long updateItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList)
            throws Exception{
        log.info("====================start:updateItem======================");
        //상품 변경
        System.out.println("상품 1 "+itemFormDto.getId());
        Item item = itemRepository.findById(itemFormDto.getId()).
                orElseThrow(EntityNotFoundException::new);
        item.updateItem(itemFormDto);
        System.out.println("상품 2 "+itemFormDto.getId());
        //상품 이미지 변경
        List<Long> itemImgIds = itemFormDto.getItemImgIds();

        for(int i =0; i<itemImgFileList.size();i++){
            itemImgService.updateItemImg(itemImgIds.get(i), itemImgFileList.get(i));
        }
        log.info("====================END:updateItem======================");
        return item.getId();
    }
    @Transactional(readOnly = true) // 쿼리문 실행 읽기만 한다.
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable){
        log.info("====================start:getAdminItemPage======================");
        return itemRepository.getAdminItemPage(itemSearchDto,pageable);
    }

    @Transactional(readOnly = true)
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable){
        log.info("====================start:getAdminItemPage======================");
        return itemRepository.getMainItemPage(itemSearchDto, pageable);
    }

//    @Transactional(readOnly = true) // 8월23일 수정
//    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
//        log.info("====================start:getMainItemPage======================");
//
//        // Item 엔티티 리스트를 가져옴
//        Page<Item> items = itemRepository.getMainItemPage(itemSearchDto, pageable);
//
//        // Item 엔티티를 MainItemDto로 변환하면서 formattedFinalPrice를 설정
//        List<MainItemDto> mainItemDtos = items.stream()
//                .map(item -> new MainItemDto(
//                        item.getId(),
//                        item.getItemNm(),
//                        item.getItemDetail(),
//                        item.getImageUrl(),
//                        item.getPrice(),
//                        item.getDiscountrate(),
//                        item.getStockNumber(),
//                        item.getFormattedFinalPrice()  // 할인된 가격을 포함하여 DTO에 설정
//                ))
//                .collect(Collectors.toList());
//
//        // Page 객체로 변환하여 반환
//        return new PageImpl<>(mainItemDtos, pageable, items.getTotalElements());
//    }


    public List<Item> getTopItems(int limit) {
        return itemRepository.findTopItemsByOrderCount(limit);
    }

    public List<ItemImg> getItemImagesByIds(List<Long> itemIds) {
        return itemRepository.findImagesByItemIds(itemIds);
    }

}
