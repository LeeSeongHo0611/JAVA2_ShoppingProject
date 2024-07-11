package com.shop.controller;

import com.shop.dto.OrderDto;
import com.shop.dto.OrderHistDto;
import com.shop.dto.PayDto;
import com.shop.entity.Order;
import com.shop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Log
public class OrderController {
    private final OrderService orderService;

//    @PostMapping(value = "/order")
//    public @ResponseBody
//    ResponseEntity order(@RequestBody @Valid OrderDto orderDto, BindingResult bindingResult,
//                         Principal principal){
//        // String a = "abc" + "def"
//        // StringBuilder a;
//        // a.append("abc");
//        // a.append("def");
//        if(bindingResult.hasErrors()){
//            StringBuilder sb = new StringBuilder();
//            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
//            for(FieldError fieldError : fieldErrors){
//                sb.append(fieldError.getDefaultMessage());
//            }
//            return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
//        }
//        // 로그인 정보 -> Spring Security
//        // principal.getName() (현재 로그인된 정보)
//        String email = principal.getName(); // a@naver.com
//        Long orderId;
//        try {
//            orderId = orderService.order(orderDto,email);
//        }catch (Exception e){
//            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
//        }
//        return new ResponseEntity<Long>(orderId, HttpStatus.OK);
//    }

    @PostMapping(value = "/KGInigisOrderValidCheck")
    public @ResponseBody ResponseEntity<Map<String, Object>> order(@RequestBody @Valid OrderDto orderDto, BindingResult bindingResult, Principal principal) {

        // 유효성 검사 오류가 있는 경우 오류 메시지를 반환 -> 오류 없을시 건너뜀
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();

            // 각 필드 오류 메시지를 StringBuilder에 추가
            for (FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage());
            }

            // 오류 메시지를 포함한 BAD_REQUEST 응답 반환
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", sb.toString());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        String email = principal.getName();
        log.info("유효성 검사 체크 완료 문제 없음. 로그인된 이메일:" + email);


        // order 추출
        Order order ;
        try {
            // orderService로 인해 덮어짐
            order = orderService.order(orderDto,email);
        }catch (Exception e){
            Map<String, Object> errorResponse = new HashMap<>();
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // KG이니지스에 지불할 PayDto 객체 생성 및 값 설정
        PayDto payDto = new PayDto();
        payDto.setMerchant_uid(order.getOrderItems().get(0).getId().toString()); // 주문 ID를 merchant_uid로 사용
        payDto.setPayName(order.getOrderItems().get(0).getItem().getItemNm()); // 첫 번째 아이템 이름 사용
        payDto.setPayAmount(String.valueOf(order.getTotalPrice())); // 총 가격 사용
        payDto.setBuyerEmail(email);//구매자 이메일
        payDto.setBuyerName(order.getMember().getName()); // 주문할 물건 이름
        payDto.setByerTel(order.getMember().getTel());
        payDto.setBuyerAddr(order.getMember().getAddress());
        payDto.setBuyerPostcode("TEST");


        // 이메일을 포함한 성공 응답 반환
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("status", "success");
        successResponse.put("PayDto", payDto);
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }


    @GetMapping(value = {"/orders", "/orders/{page}"})
    public String orderHist(@PathVariable("page") Optional<Integer> page, Principal principal, Model model){
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 5);

        Page<OrderHistDto> orderHistDtoList = orderService.getOrderList(principal.getName(), pageable);

        model.addAttribute("orders", orderHistDtoList);
        model.addAttribute("page", pageable.getPageNumber());
        model.addAttribute("maxPage",5);
        return "order/orderHist";
    }

    @PostMapping("/order/{orderId}/cancel")
    public @ResponseBody ResponseEntity cancelOrder(@PathVariable("orderId") Long orderId, Principal principal){
        if(!orderService.validateOrder(orderId, principal.getName())){
            return new ResponseEntity<String>("주문 취소 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }
        orderService.cancelOrder(orderId);
        return new ResponseEntity<Long>(orderId, HttpStatus.OK);
    }

}
