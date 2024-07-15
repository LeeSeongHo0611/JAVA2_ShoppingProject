package com.shop.controller;

import com.shop.dto.NoticeBoardDto;
import com.shop.dto.NoticeBoardSearchDto;
import com.shop.entity.NoticeBoard;
import com.shop.service.NoticeBoardService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    public String noticeBdDtl(@PathVariable("noticeBdId") Long noticeBdId, Model model, HttpServletRequest request,
                              HttpServletResponse response) {
        try {
            // 쿠키에서 조회한 게시글 ID 목록 가져오기
            Cookie[] cookies = request.getCookies();
            String newCookie = noticeBdId + "_";
            boolean viewed = false;

            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("views")) {
                        // 기존 쿠키 값을 가져와서 확인
                        String cookieValue = cookie.getValue();
                        if (cookieValue.contains(newCookie)) {
                            viewed = true;
                            break;
                        }
                    }
                }
            }

            // 이미 조회한 적이 없는 경우에만 조회수 증가 및 쿠키 추가
            if (!viewed) {
                noticeBoardService.incrementViews(noticeBdId); // 조회수 증가
                StringBuilder updatedCookieValue = new StringBuilder(newCookie);
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if (cookie.getName().equals("views")) {
                            updatedCookieValue.insert(0, cookie.getValue()); // 기존 값 앞에 추가
                            break;
                        }
                    }
                }
                // 새로운 쿠키 생성 및 설정
                Cookie cookie = new Cookie("views", updatedCookieValue.toString());
                cookie.setMaxAge(24 * 60 * 60); // 쿠키 유효 기간 설정 (24시간)
                cookie.setPath("/"); // 쿠키 경로 설정 (사이트 전역에서 사용)
                response.addCookie(cookie); // 쿠키 추가
            }

            // 게시글 상세 정보 가져오기
            NoticeBoardDto noticeBoardDto = noticeBoardService.getNoticeBdDtl(noticeBdId);
            model.addAttribute("noticeBoardDto", noticeBoardDto);

        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "존재하지 않는 게시물입니다.");
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
