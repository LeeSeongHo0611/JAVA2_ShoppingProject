package com.shop.controller;

import com.shop.dto.ItemFormDto;
import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log
@Controller
@RequiredArgsConstructor // final, @nonNull 로 선언된 필드 포함하는 생성자를 자동생성
public class ItemController {
    private final ItemService itemService;

    private final DiscountService discountService; // 8월19일 추가
//


    @GetMapping(value = "/admin/item/new") // 상품등록페이지
    public String itemForm(Model model){
        log.info("====================Start:/admin/item/new -> GetMapping======================");
        model.addAttribute("itemFormDto",new ItemFormDto());
        log.info("====================END:/admin/item/new -> GetMapping======================");
        return "item/itemForm";
    }

    @PostMapping(value = "/admin/item/new") // 상품 입력후 등록버튼
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

    @GetMapping(value = "/admin/item/{itemId}") // 상품수정
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

    @PostMapping(value = "/admin/item/{itemId}") // 상품수정후 수정버튼
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
    @GetMapping(value = {"/admin/items", "/admin/items/{page}"}) // 상품관리페이지
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

    @GetMapping(value = "/item/{itemId}") // 상품세부정보 페이지
    public String itemDtl(Model model, @PathVariable("itemId")Long itemId){
        log.info("==================== Start:/item/{itemId} -> GetMapping ======================");
        ItemFormDto itemFormDto = itemService.getItemDtl(itemId);

        // 최종 가격 계산 추가 8월26일 int로 변경 9월3일
        int finalPrice = discountService.calculateFinalPrice(itemFormDto.createItem());
        model.addAttribute("item",itemFormDto);
        log.info("==================== END:/item/{itemId} -> GetMapping ======================");
        return "item/itemDtl";
    }
//
//    @GetMapping("/item/{id}") // 8월20일 수정
//    public String getItem(@PathVariable("id") Long id, Model model) { // 상품상세정보
//
//        log.info("Start: getItem() - id: " + id); // 메소드 시작 시 로그 8월20일추가
//
//
//        Item item = itemService.getItemById(id);
//        BigDecimal finalPrice = discountService.calculateFinalPrice(item);
//
//        MainItemDto mainItemDto = new MainItemDto(
//                item.getId(),
//                item.getItemNm(),
//                item.getItemDetail(),
//                null, // imgUrl은 필요에 따라 설정하세요
//                item.getPrice(),
//                item.getDiscountrate(),
//                item.getStockNumber(),
//                finalPrice // 최종 가격 전달
//        );
//
//        model.addAttribute("item", mainItemDto);
//        log.info("End: getItem() - MainItemDto: " + mainItemDto); // 메소드 종료 시 로그 8월20일추가
//        log.info("Model item class: " + model.getAttribute("item").getClass().getName()); // 모델아이템클래스 item객체확인 로그 8월21일
//
//        return "item/itemDetail";
//    }

//    // 사용자 상품 상세 정보 보기 8월26일 위의 두 메소드 합침
//    @GetMapping("/item/{itemId}")
//    public String ItemDetails(@PathVariable("itemId") Long itemId, Model model) { // 상품수정 쪽과 메소드 겹쳐서 메소드이름변경
//        log.info("Start: ItemDetails() - id: " + itemId); // 메소드 시작 시 로그
//
//        // 기존 Item 객체를 가져옴
//        Item item = itemService.getItemById(itemId);
//        // ItemFormDto로 변환 8월26일
//        ItemFormDto itemFormDto = ItemFormDto.of(item);
//
//        BigDecimal finalPrice = discountService.calculateFinalPrice(item);
//
//        // 최종 가격 설정
//        itemFormDto.setPrice(finalPrice);
//
//        //모델에 itemFormDto 추가
//        model.addAttribute("item", itemFormDto);
//        log.info("End: ItemDetails() - MainItemDto: " + itemFormDto); // 메소드 종료 시 로그
//
//        return "item/itemDtl";
//    }



    @GetMapping(value = "/bestItem") // 베스트상품 목록
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