package com.shop.controller;

import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
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

@Controller
@RequiredArgsConstructor
public class MainController {
    private final ItemService itemService;
    private final MainService mainService;

    @GetMapping(value = "/")
    public String main(ItemSearchDto itemSearchDto, Optional<Integer> page, Model model) throws IOException {
        Pageable pageable = PageRequest.of(page.orElse(0), 5);
        Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, pageable);

        // 이미지 리스트를 서비스에서 가져옴
        List<String> mainImages = mainService.getMainImages();

        // 모델에 이미지 리스트 추가
        model.addAttribute("mainImages", mainImages);

        model.addAttribute("items", items);
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("maxPage", 5);
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