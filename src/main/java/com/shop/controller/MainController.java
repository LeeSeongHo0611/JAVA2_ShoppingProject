package com.shop.controller;

import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
import com.shop.entity.Item;
import com.shop.entity.ItemImg;
import com.shop.service.DiscountService;
import com.shop.service.ItemService;
import com.shop.service.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final ItemService itemService;
    private final MainService mainService;

//    @GetMapping(value = "/")
//    public String main(ItemSearchDto itemSearchDto, Optional<Integer> page, Model model) throws IOException {
//        Pageable pageable = PageRequest.of(page.orElse(0), 5);
//        Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, pageable);
//
//        // 이미지 리스트를 서비스에서 가져옴
//        List<String> mainImages = mainService.getMainImages();
//
//        // 모델에 이미지 리스트 추가
//        model.addAttribute("mainImages", mainImages);
//
//        // MainItemDto 객체 제대로 생성되는지 확인용 로그 추가 8월21일
//        if (items.hasContent()) {
//            items.getContent().forEach(item -> {
//                System.out.println("Item Name: " + item.getItemNm());
//                System.out.println("Final Price: " + item.getFinalPrice());
//                System.out.println("Item Class: " + item.getClass().getName());
//            });
//        } else {
//            System.out.println("No items found.");
//        }
//
//        model.addAttribute("items", items);
//        model.addAttribute("itemSearchDto", itemSearchDto);
//        model.addAttribute("maxPage", 5);
//
//        //베스트아이탬
//        int limit = 5;
//        List<Item> itemBest = itemService.getTopItems(limit);
//
//        // ID 리스트 추출
//        List<Long> itemIds = itemBest.stream()
//                .map(Item::getId)
//                .collect(Collectors.toList());
//
//        List<ItemImg> itemImages = itemService.getItemImagesByIds(itemIds); // 해당 ID에 맞는 이미지 가져오기
//
//        model.addAttribute("itemBest", itemBest);
//        model.addAttribute("itemImages", itemImages); // 이미지 정보 추가
//        System.out.println(itemBest+"베스트아이탬");
//        System.out.println(itemImages+"아이탬이미지");
//        System.out.println("break");
//        return "main";

    private final DiscountService discountService; // DiscountService를 주입 8월22일

    @GetMapping(value = "/") // 8월22일 수정
    public String main(ItemSearchDto itemSearchDto, Optional<Integer> page, Model model) throws IOException {
        Pageable pageable = PageRequest.of(page.orElse(0), 5);
        Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, pageable);

        // 이미지 리스트를 서비스에서 가져옴
        List<String> mainImages = mainService.getMainImages();

        // 모델에 이미지 리스트 추가
        model.addAttribute("mainImages", mainImages);

        // 각 아이템에 대해 할인된 최종 가격을 계산하고 설정 8월22일
        items.getContent().forEach(item -> {
            Item itemEntity = itemService.findItemById(item.getId()); // item의 ID를 이용해 Item 엔티티를 가져옴
            item.setFinalPrice(discountService.calculateFinalPrice(itemEntity)); // 할인된 최종 가격을 설정
        });

        // MainItemDto 객체 제대로 생성되는지 확인용 로그 추가 8월21일
        if (items.hasContent()) {
            items.getContent().forEach(item -> {
                System.out.println("Item Name: " + item.getItemNm());
                System.out.println("Final Price: " + item.getFinalPrice());
                System.out.println("Item Class: " + item.getClass().getName());
            });
        } else {
            System.out.println("No items found.");
        }

        model.addAttribute("items", items);
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("maxPage", 5);

        // 베스트아이템
        int limit = 5;
        List<Item> itemBest = itemService.getTopItems(limit);

        // ID 리스트 추출
        List<Long> itemIds = itemBest.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        List<ItemImg> itemImages = itemService.getItemImagesByIds(itemIds); // 해당 ID에 맞는 이미지 가져오기

        // 베스트아이템에 대해 할인된 최종 가격을 계산하고 설정
        itemBest.forEach(item -> item.setFinalPrice(discountService.calculateFinalPrice(item)));

        model.addAttribute("itemBest", itemBest);
        model.addAttribute("itemImages", itemImages); // 이미지 정보 추가
        System.out.println(itemBest + " 베스트아이템");
        System.out.println(itemImages + " 아이템이미지");
        System.out.println("break");
        return "main";
    }


    @GetMapping(value = "/loadItems")
    @ResponseBody
    public Page<MainItemDto> loadItems(ItemSearchDto itemSearchDto, @RequestParam("page") int page) {
        System.out.println("AJAX 통신 시작, 페이지: " + page);
        Pageable pageable = PageRequest.of(page, 5);
        return itemService.getMainItemPage(itemSearchDto, pageable);
    }

    @GetMapping(value = "/mapApi/comingRoute")
    public String coming(){
        return "mapApi/comingRoute";
    }

}