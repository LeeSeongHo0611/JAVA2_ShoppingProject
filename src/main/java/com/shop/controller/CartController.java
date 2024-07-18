package com.shop.controller;

import com.shop.dto.CartDetailDto;
import com.shop.dto.CartItemDto;
import com.shop.dto.CartOrderDto;
import com.shop.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping(value = "/cart") // HTTP POST 요청을 처리하는 엔드포인트로 지정
    public @ResponseBody
    ResponseEntity order(@RequestBody @Valid CartItemDto cartItemDto,
                         BindingResult bindingResult, Principal principal) {

        // 입력 데이터 검증 에러가 있는 경우
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for (FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage());
            }
            // 에러 메시지를 포함한 400 BAD REQUEST 응답 반환
            return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
        }

        // 현재 인증된 사용자의 이메일 주소를 가져옴
        String email = principal.getName();
        Long cartItemId;


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

        //********************************************
        try {
            // cartService를 통해 장바구니에 항목을 추가하고, 추가된 항목의 ID를 반환받음
            cartItemId = cartService.addCart(cartItemDto, email);
        } catch (Exception e) {
            // 에러가 발생한 경우 에러 메시지를 포함한 400 BAD REQUEST 응답 반환
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        // 성공적으로 항목이 추가된 경우 항목 ID를 포함한 200 OK 응답 반환
        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

    @GetMapping(value = "/cart")
    public String orderHist(Principal principal, Model model){
        String email = null;
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

            // 소셜 로그인 사용자의 이메일을 사용하여 장바구니 리스트를 가져옴
            cartDetailDtoList = cartService.getCartList(email);
        } else if (principal instanceof UsernamePasswordAuthenticationToken) {
            // 일반 스프링 로그인인 경우
            // UsernamePasswordAuthenticationToken을 사용하여 사용자 이름을 가져옴
            UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) principal;
            String username = authToken.getName();

            // 일반 로그인 사용자의 이름을 사용하여 장바구니 리스트를 가져옴
            cartDetailDtoList = cartService.getCartList(username);
        } else {
            // 일반 로그인 소셜로그인 모두 오류인 경우
            throw new IllegalArgumentException("Unexpected principal type");
        }

        model.addAttribute("cartItems", cartDetailDtoList);
        return "cart/cartList";
    }
    @PatchMapping(value = "/cartItem/{cartItemId}")
    public @ResponseBody ResponseEntity updateCartItem(@PathVariable("cartItemId") Long cartItemId,
                                                       int count, Principal principal) {
        System.out.println(cartItemId);
        if (count <= 0) {
            return new ResponseEntity<String>("최소 1개이상 담아주세요.", HttpStatus.BAD_REQUEST);
        } else if (!cartService.validateCartItem(cartItemId, principal.getName())) {
            return new ResponseEntity<String>("수정권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        cartService.updateCartItemCount(cartItemId, count);
        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

    @DeleteMapping(value = "/cartItem/{cartItemId}")
    public @ResponseBody ResponseEntity deleteCartItem(@PathVariable("cartItemId") Long cartItemId,
                                                       Principal principal){
        if (!cartService.validateCartItem(cartItemId, principal.getName())) {
            return new ResponseEntity<String>("수정권한이 없습니다.", HttpStatus.FORBIDDEN);
        }
        cartService.deleteCartItem(cartItemId);
        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

    @PostMapping(value = "/cart/orders")
    public @ResponseBody ResponseEntity orderCartItem(@RequestBody CartOrderDto cartOrderDto,
                                                      Principal principal){
        System.out.println(cartOrderDto.getCartItemId());
        //CartOrderDtoList List <- getCartOrderDtoList 통해서 Views 내랴운 리스트
        List<CartOrderDto> cartOrderDtoList = cartOrderDto.getCartOrderDtoList();
        // null, size가 0이면 실행
        if(cartOrderDtoList == null || cartOrderDtoList.size() == 0){
            return new ResponseEntity<String>("주문할 상품을 선택해주세요.",HttpStatus.FORBIDDEN);
        }
        // 전체 유효성검사
        for(CartOrderDto cartOrder : cartOrderDtoList){
            if(!cartService.validateCartItem(cartOrder.getCartItemId(), principal.getName())){
                return new ResponseEntity<String>("주문 권한이 없습니다.",HttpStatus.FORBIDDEN);
            }
        }

        Long orderId;
        try {
            // cart -> order
            // cartService -> orderService
            // cartOrderDtoList(CartOrderDtoList)
            orderId = cartService.orderCartItem(cartOrderDtoList, principal.getName());
        }
        catch (Exception e){ // 실패하면
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        //성공
        return new ResponseEntity<Long>(orderId,HttpStatus.OK);
    }
}
