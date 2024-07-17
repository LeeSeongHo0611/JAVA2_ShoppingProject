package com.shop.controller;

import com.shop.dto.CartDetailDto;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
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

        //스프링으로 로그인했는지 소셜로 로그인했는지 에 따른 값 추출
        String email;

        if (principal instanceof OAuth2AuthenticationToken) {
            // 소셜 로그인인 경우
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) principal;
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            String registrationId = oauth2Token.getAuthorizedClientRegistrationId();

            if ("kakao".equals(registrationId)) {
                // 카카오 로그인 사용자의 이메일 추출
                Map<String, Object> kakaoAccount = (Map<String, Object>) oauth2User.getAttributes().get("kakao_account");
                email = (String) kakaoAccount.get("email");
            } else if ("naver".equals(registrationId)) {
                // 네이버 로그인 사용자의 이메일 추출
                Map<String, Object> naverAccount = (Map<String, Object>) oauth2User.getAttributes().get("response");
                email = (String) naverAccount.get("email");
            } else if ("google".equals(registrationId)) {
                // 구글 로그인 사용자의 이메일 추출
                email = (String) oauth2User.getAttributes().get("email");
            } else {
                throw new IllegalArgumentException("Unexpected registration id: " + registrationId);
            }


        } else if (principal instanceof UsernamePasswordAuthenticationToken) {
            // 일반 스프링 로그인인 경우의 이메일 추출
            // UsernamePasswordAuthenticationToken을 사용하여 사용자 이름을 가져옴
            UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) principal;
            email = authToken.getName();

        } else {
            // 일반 로그인 소셜로그인 모두 오류인 경우
            throw new IllegalArgumentException("Unexpected principal type");
        }

        log.info("유효성 검사 체크 완료 문제 없음. 로그인된 이메일:" + email);


        // 주문서 생성 및 결제 PayDto 생성
        Order order ;
        try {
            // 주문서 생성
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

    // KG 이니지스 결제 취소 - JSON : 주문서 내역을 삭제
    @DeleteMapping("/KGInigisOrderDELETE")
    public @ResponseBody ResponseEntity<?> KGInigisOrderCancel(@RequestBody Map<String, Long> requestBody, Principal principal) {
        Long orderId = requestBody.get("orderId");
        // 이전 주문 재고 카운트 롤백
        orderService.cancelOrder(orderId);
        // 주문 취소 -> 주문내역 삭제
        orderService.deleteOrderById(orderId);
        return ResponseEntity.ok("Order deleted successfully");
    }



    @GetMapping(value = {"/orders", "/orders/{page}"})
    public String orderHist(@PathVariable("page") Optional<Integer> page, Principal principal, Model model){
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 5);

        // 내부 shop 로그인, 소셜 로그인에 따른 이메일 추출
        String email = "";
        List<CartDetailDto> cartDetailDtoList = null;

        if (principal instanceof OAuth2AuthenticationToken) {
            // 소셜 로그인인 경우
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) principal;
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            String registrationId = oauth2Token.getAuthorizedClientRegistrationId();

            if ("kakao".equals(registrationId)) {
                // 카카오 로그인 사용자의 이메일 추출
                Map<String, Object> kakaoAccount = (Map<String, Object>) oauth2User.getAttributes().get("kakao_account");
                email = (String) kakaoAccount.get("email");
            } else if ("naver".equals(registrationId)) {
                // 네이버 로그인 사용자의 이메일 추출
                Map<String, Object> naverAccount = (Map<String, Object>) oauth2User.getAttributes().get("response");
                email = (String) naverAccount.get("email");
            } else if ("google".equals(registrationId)) {
                // 구글 로그인 사용자의 이메일 추출
                email = (String) oauth2User.getAttributes().get("email");
            } else {
                throw new IllegalArgumentException("Unexpected registration id: " + registrationId);
            }



        } else if (principal instanceof UsernamePasswordAuthenticationToken) {
            // 일반 스프링 로그인인 경우
            // UsernamePasswordAuthenticationToken을 사용하여 사용자 이름을 가져옴
            UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) principal;
             email = authToken.getName();


        } else {
            // 일반 로그인 소셜로그인 모두 오류인 경우
            throw new IllegalArgumentException("Unexpected principal type");
        }

        Page<OrderHistDto> orderHistDtoList = orderService.getOrderList(email, pageable);

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
