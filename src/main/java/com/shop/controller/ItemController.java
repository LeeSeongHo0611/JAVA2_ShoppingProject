package com.shop.controller;

import com.shop.dto.ItemFormDto;
import com.shop.dto.ItemSearchDto;
import com.shop.entity.Item;
import com.shop.entity.ItemImg;
import com.shop.service.ItemService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@Controller
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    @GetMapping(value = "/admin/item/new")
    public String itemForm(Model model){
        model.addAttribute("itemFormDto",new ItemFormDto());
        return "item/itemForm";
    }
    @PostMapping(value = "/admin/item/new")
    public String itemNew(@Valid ItemFormDto itemFormDto, BindingResult bindingResult, Model model,
                          @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList){
        if(bindingResult.hasErrors()){
            return "item/itemForm";
        }
        if(itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null){
            model.addAttribute("errorMessage",
                    "첫번째 상품 이미지는 필수 입력 값입니다.");
            return "item/itemForm";
        }
        try {
            itemService.saveItem(itemFormDto, itemImgFileList);
        }catch (Exception e){
            model.addAttribute("errorMessage",
                    "상품 등록 중 에러가 발생하였습니다.");
            return "item/itemForm";
        }
        return "redirect:/";
    }

    @GetMapping(value = "/admin/item/{itemId}")
    public String itemDtl(@PathVariable("itemId")Long itemId, Model model){
        try {
            ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
            model.addAttribute("itemFormDto", itemFormDto);
        }catch (EntityNotFoundException e){
            model.addAttribute("errorMessage","존재하지 않는 상품입니다.");
            model.addAttribute("itemFormDto",new ItemFormDto());
            return "item/itemForm";
        }

        return "item/itemForm";
    }

    @PostMapping(value = "/admin/item/{itemId}")
    public String itemUpdate(@Valid ItemFormDto itemFormDto, BindingResult bindingResult,
                             @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList,
                             Model model){
        System.out.println("상품 3 "+itemFormDto.getId());
        if(bindingResult.hasErrors()){
            return "item/itemForm";
        }
        System.out.println("상품 4 "+itemFormDto.getId());
        if(itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null){
            model.addAttribute("errorMessage", "첫번째 상품 이미지는 필수 입력 값입니다.");
            return "item/itemForm";
        }
        try {
            System.out.println("상품 5 "+itemFormDto.getId());
            itemService.updateItem(itemFormDto, itemImgFileList);
            System.out.println("상품 6 "+itemFormDto.getId());
        }catch (Exception e){
            model.addAttribute("errorMessage", "상품 수정 중 에러가 발생하였습니다.");
            return "item/itemForm";
        }
        return "redirect:/"; // 다시 실행 /
    }

    //value 2개인 이유
    //1. 네비게이션에서 상품관리 클릭하면 나오는거
    //2. 상품관리안에서 페이지 이동할 때 받는거
    @GetMapping(value = {"/admin/items", "/admin/items/{page}"})
    public String itemManage(ItemSearchDto itemSearchDto, @PathVariable("page") Optional<Integer> page,
                             Model model){
        // page.isPresent() -> page 값 있어?
        // 어 값 있어 page.get()  아니 값 없어 0
        // 페이지당 사이즈 5 -> 5개만나옵니다. 6개 되면 페이지 바뀌죠
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 5);

        Page<Item> items = itemService.getAdminItemPage(itemSearchDto, pageable);
        model.addAttribute("items", items);
        model.addAttribute("itemSearchDto",itemSearchDto);
        model.addAttribute("maxPage",5);
        return "item/itemMng";
    }

    @GetMapping(value = "/item/{itemId}")
    public String itemDtl(Model model, @PathVariable("itemId")Long itemId){
        ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
        model.addAttribute("item",itemFormDto);
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