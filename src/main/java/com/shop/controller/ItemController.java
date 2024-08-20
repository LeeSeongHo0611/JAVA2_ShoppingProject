package com.shop.controller;

import com.shop.dto.ItemFormDto;
import com.shop.dto.ItemSearchDto;
import com.shop.entity.Item;
import com.shop.entity.ItemImg;
import com.shop.service.DiscountService;
import com.shop.service.ItemService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log
@Controller
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    private final DiscountService discountService; // 8월19일 추가
//
    public ItemController(DiscountService discountService) {
        this.discountService = discountService;
    }

    @GetMapping("/item/{id}")
    public String getItem(@PathVariable("id") Long id, Model model) {
        Item item = itemService.getItemById(id);
        BigDecimal finalPrice = discountService.calculateFinalPrice(item);
        model.addAttribute("item", item);
        model.addAttribute("finalPrice", finalPrice);
        return "item/itemDetail";
    }

    @GetMapping(value = "/admin/item/new")
    public String itemForm(Model model){
        log.info("====================Start:/admin/item/new -> GetMapping======================");
        model.addAttribute("itemFormDto",new ItemFormDto());
        log.info("====================END:/admin/item/new -> GetMapping======================");
        return "item/itemForm";
    }

    @PostMapping(value = "/admin/item/new")
    public String itemNew(@Valid ItemFormDto itemFormDto, BindingResult bindingResult, Model model,
                          @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList){
        log.info("====================Start:/admin/item/new -> GetMapping======================");
        if(bindingResult.hasErrors()){
            log.info("====================END:/admin/item/new -> GetMapping======================");
            return "item/itemForm";
        }
        if(itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null){
            model.addAttribute("errorMessage",
                    "첫번째 상품 이미지는 필수 입력 값입니다.");
            log.info("====================END:/admin/item/new -> GetMapping======================");
            return "item/itemForm";
        }
        try {
            itemService.saveItem(itemFormDto, itemImgFileList);
        }catch (Exception e){
            model.addAttribute("errorMessage",
                    "상품 등록 중 에러가 발생하였습니다.");
            log.info("====================END:/admin/item/new -> GetMapping======================");
            return "item/itemForm";
        }
        log.info("====================END:/admin/item/new -> GetMapping======================");
        return "redirect:/";
    }

    @GetMapping(value = "/admin/item/{itemId}")
    public String itemDtl(@PathVariable("itemId")Long itemId, Model model){
        log.info("====================Start:/admin/item/{itemId} -> GetMapping======================");
        try {
            ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
            model.addAttribute("itemFormDto", itemFormDto);
        }catch (EntityNotFoundException e){
            model.addAttribute("errorMessage","존재하지 않는 상품입니다.");
            model.addAttribute("itemFormDto",new ItemFormDto());
            log.info("====================END:/admin/item/{itemId} -> GetMapping======================");
            return "item/itemForm";
        }
        log.info("====================END:/admin/item/{itemId} -> GetMapping======================");
        return "item/itemForm";
    }

    @PostMapping(value = "/admin/item/{itemId}")
    public String itemUpdate(@Valid ItemFormDto itemFormDto, BindingResult bindingResult,
                             @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList,
                             Model model){
        log.info("====================Start:/admin/item/{itemId} -> PostMapping======================");
        if(bindingResult.hasErrors()){
            log.info("====================END:/admin/item/{itemId} -> PostMapping======================");
            return "item/itemForm";
        }
        if(itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null){
            model.addAttribute("errorMessage", "첫번째 상품 이미지는 필수 입력 값입니다.");
            log.info("====================END:/admin/item/{itemId} -> PostMapping======================");
            return "item/itemForm";
        }
        try {
            itemService.updateItem(itemFormDto, itemImgFileList);
        }catch (Exception e){
            model.addAttribute("errorMessage", "상품 수정 중 에러가 발생하였습니다.");
            log.info("====================END:/admin/item/{itemId} -> PostMapping======================");
            return "item/itemForm";
        }
        log.info("====================END:/admin/item/{itemId} -> PostMapping======================");
        return "redirect:/"; // 다시 실행 /
    }

    //value 2개인 이유
    //1. 네비게이션에서 상품관리 클릭하면 나오는거
    //2. 상품관리안에서 페이지 이동할 때 받는거
    @GetMapping(value = {"/admin/items", "/admin/items/{page}"})
    public String itemManage(ItemSearchDto itemSearchDto, @PathVariable("page") Optional<Integer> page,
                             Model model){
        log.info("==================== Start:/admin/items , /admin/items/{page} -> GetMapping ======================");
        // page.isPresent() -> page 값 있어?
        // 어 값 있어 page.get()  아니 값 없어 0
        // 페이지당 사이즈 5 -> 5개만나옵니다. 6개 되면 페이지 바뀌죠
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 5);

        Page<Item> items = itemService.getAdminItemPage(itemSearchDto, pageable);
        model.addAttribute("items", items);
        model.addAttribute("itemSearchDto",itemSearchDto);
        model.addAttribute("maxPage",5);
        log.info("==================== END:/admin/items , /admin/items/{page} -> GetMapping ======================");
        return "item/itemMng";
    }

    @GetMapping(value = "/item/{itemId}")
    public String itemDtl(Model model, @PathVariable("itemId")Long itemId){
        log.info("==================== Start:/item/{itemId} -> GetMapping ======================");
        ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
        model.addAttribute("item",itemFormDto);
        log.info("==================== END:/item/{itemId} -> GetMapping ======================");
        return "item/itemDtl";
    }

    @GetMapping(value = "/bestItem")
    public String itemBest(Model model) {
        int limit = 5;
        List<Item> itemBest = itemService.getTopItems(limit);

        // ID 리스트 추출
        List<Long> itemIds = itemBest.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        List<ItemImg> itemImages = itemService.getItemImagesByIds(itemIds); // 해당 ID에 맞는 이미지 가져오기

        model.addAttribute("itemBest", itemBest);
        model.addAttribute("itemImages", itemImages); // 이미지 정보 추가
        System.out.println(itemBest+"베스트아이탬");
        System.out.println(itemImages+"아이탬이미지");
        System.out.println("break");
        return "item/itemBest";

    }


}