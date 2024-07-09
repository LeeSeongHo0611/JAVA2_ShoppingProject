package com.shop.controller;

import com.shop.dto.NoticeBoardDto;
import com.shop.dto.NoticeBoardSearchDto;
import com.shop.entity.NoticeBoard;
import com.shop.service.NoticeBoardService;
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


import java.util.Optional;

@Log
@Controller
@RequiredArgsConstructor
public class NoticeBoardController {

    private final NoticeBoardService noticeBoardService;

    @GetMapping(value = "/boards/newBd")
    public String BdForm(Model model) {
        System.out.println("문제 1");
        model.addAttribute("noticeBoardDto", new NoticeBoardDto());
        System.out.println("anspw2");
        return "noticeBoard/noticeBdForm";
    }
    @PostMapping(value = "/boards/newBd")
    public String bdNew(@Valid NoticeBoardDto noticeBoardDto, BindingResult bindingResult, Model model) {
        System.out.println("answp3");
        if (bindingResult.hasErrors()) {
            System.out.println("answp4");
            return "noticeBoard/noticeBdForm";
        }
        try {
            noticeBoardService.saveNoticeBd(noticeBoardDto);
        }catch (Exception e){
            model.addAttribute("errorMessage",
                    "상품 등록 중 에러가 발생하였습니다.");
            return "noticeBoard/noticeBdForm";
        }
        System.out.println("answp5");
        return "redirect:/boards/notice"; //게시글 홈페이지로 이동하게끔 수정
    }

    @GetMapping(value = "/boards/newBd/{noticeBdId}")
    public String noticeBdDtl(@PathVariable("noticeBdId")Long noticeBdId, Model model){
        try {
            NoticeBoardDto noticeBoardDto = noticeBoardService.getNoticeBdDtl(noticeBdId);
            model.addAttribute("noticeBoardDto", noticeBoardDto);
            System.out.println(noticeBoardDto.getId()+"????ADADAD");
            System.out.println(noticeBdId);
        }catch (EntityNotFoundException e){
            model.addAttribute("errorMessage", "존재 하지 않는 게시물 입니다.");
            model.addAttribute("noticeBoardDto", new NoticeBoardDto());
            return "noticeBoard/noticeBdForm";
        }
        return "noticeBoard/noticeBdForm";
    }

    @PostMapping(value = "/boards/newBd/{noticeBdId}")
    public String noticeBdUpdate(@Valid NoticeBoardDto noticeBoardDto, BindingResult bindingResult,
                                 Model model, @PathVariable("noticeBdId")Long noticeBdId){

        System.out.println(noticeBoardDto.getTitle()+"@!@!@");
        System.out.println(noticeBoardDto.getId()+"@!@!@");
        if (bindingResult.hasErrors()){
            return "noticeBoard/noticeBdForm";
        }
        try {

            NoticeBoardDto existingNoticeBoardDto = noticeBoardService.getNoticeBdDtl(noticeBdId);
            existingNoticeBoardDto.setTitle(noticeBoardDto.getTitle());
            existingNoticeBoardDto.setContent(noticeBoardDto.getContent());
            System.out.println("수정 1 ");
            noticeBoardService.updateNoticeBd(existingNoticeBoardDto);
            System.out.println("수정 2 ");
        }catch (Exception e){
            model.addAttribute("errorMessage","게시글 수정중 오류가 발생 하였습니다.");
            return "noticeBoard/noticeBdForm";
        }
        return "redirect:/boards/notice"; //게시글 페이지로 이동하게끔 수정하기
    }

    @GetMapping(value = {"/boards/notice","/boards/notice/{page}"})
    public String noticeManage(NoticeBoardSearchDto noticeBoardSearchDto, @PathVariable("page")Optional<Integer> page,
                               Model model){
        System.out.println("보드 1"+page+"보드1"+noticeBoardSearchDto);
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 5);
        System.out.println("보드 2"+page+"보드1"+noticeBoardSearchDto);
        Page<NoticeBoard> noticeBoards = noticeBoardService.getAdminNoticeBdPage(noticeBoardSearchDto, pageable);
        System.out.println("보드 3"+page+"보드1"+noticeBoardSearchDto);
        model.addAttribute("noticeBoards", noticeBoards);
        model.addAttribute("noticeBoardSearchDto", noticeBoardSearchDto);
        model.addAttribute("maxPage", 5);
        System.out.println("보드 4"+page+"보드1"+noticeBoardSearchDto);

        return "noticeBoard/noticeBdMng";
    }

    @GetMapping(value = "/notice/{noticeBdId}")
    public String noticeBdDtl(Model model, @PathVariable("noticeBdId")Long noticeBdId){
        System.out.println(noticeBdId+"1a");
        NoticeBoardDto noticeBoardDto = noticeBoardService.getNoticeBdDtl(noticeBdId);
        System.out.println(noticeBdId+"2a");
        model.addAttribute("notice", noticeBoardDto);
        System.out.println(noticeBdId+"3a");
        return "noticeBoard/noticeBdMng";
    }
}
