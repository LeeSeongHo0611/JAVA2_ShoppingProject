package com.shop.service;

import com.shop.dto.CartDetailDto;
import com.shop.dto.CartItemDto;
import com.shop.dto.CartOrderDto;
import com.shop.dto.OrderDto;
import com.shop.entity.Cart;
import com.shop.entity.CartItem;
import com.shop.entity.Item;
import com.shop.entity.Member;
import com.shop.repository.CartItemRepository;
import com.shop.repository.CartRepository;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Log
public class CartService {
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderService orderService;
    private final DiscountService discountService; // DiscountService 주입 8월27일

    public Long addCart(CartItemDto cartItemDto, String email){
        log.info("=========Start========");
        Item item = itemRepository.findById(cartItemDto.getItemId())
                .orElseThrow(EntityExistsException::new);
        Member member = memberRepository.findByEmail(email);

        Cart cart = cartRepository.findByMemberId(member.getId());
        if(cart == null){
            cart = Cart.createCart(member);
            cartRepository.save(cart);
        }

        CartItem savedCartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(),item.getId());
        if(savedCartItem != null){
            savedCartItem.addCount(cartItemDto.getCount());
            return savedCartItem.getId();
        }
        else{
            CartItem cartItem = CartItem.createCartItem(cart, item, cartItemDto.getCount());
            cartItemRepository.save(cartItem);
            return cartItem.getId();
        }
    }
    @Transactional(readOnly = true)
    public List<CartDetailDto> getCartList(String email){
        log.info("=========Start========");
        List<CartDetailDto> cartDetailDtoList = new ArrayList<>();

        Member member = memberRepository.findByEmail(email); // 사용자의 이메일로 Member객체 조회

        Cart cart = cartRepository.findByMemberId(member.getId()); // 사용자의 장바구니 조회
        if(cart == null){
            return cartDetailDtoList;
        }

        // 장바구니 항목들을 불러옵니다 장바구니 항목들 조회
        cartDetailDtoList = cartItemRepository.findCartDetailDtoList(cart.getId());

        // 각 항목의 최종 가격을 DiscountService를 통해 계산합니다. 8월27일 추가
        for (CartDetailDto cartItem : cartDetailDtoList) {
            Item item = itemRepository.findById(cartItem.getItemId())
                    .orElseThrow(EntityExistsException::new);
            int finalPrice = discountService.calculateFinalPrice(item); // item의 원래가격을 기준으로 할인된 최종가격 계산 totalPrice += orderItem.getTotalPrice();
            cartItem.setFinalPrice(finalPrice); // 할인된 가격으로 장바구니에 설정 8월27일 추가
            
            //8월28일 추가
            String formattedFinalPrice = finalPrice + "원"; // 문자열 포맷팅
            // 템플릿에서 바로 사용하기 위해 cartItem에 새로운 필드를 추가하지 않고 포맷팅된 문자열을 생성
            cartItem.setFinalPrice(finalPrice); // 만약 이 값이 그대로 필요하다면 설정
            // 여기서 formattedFinalPrice를 사용하는 대신, 템플릿에서 직접 포맷팅하도록 처리
        }

        return cartDetailDtoList; // 할인된가격이 적용된 리스트 반환
    }

    @Transactional(readOnly = true)
    public boolean validateCartItem(Long cartItemId, String email){
        log.info("=========Start========");
        // email을 이용해서 Member 엔티티 객체 추출
        Member curMember = memberRepository.findByEmail(email);
        // cartItemId를 이용해서 CartItem 엔티티 객체 추출
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityExistsException::new);
        // Cart -> Memeber 엔티티 객체를 추출
        Member savedMember = cartItem.getCart().getMember();
        // 현재 로그인된 Member == CartItem에 있는 Member -> 같지 않으면 true return false
        if(!StringUtils.equals(curMember.getEmail(),savedMember.getEmail())){
            return false;
        }
        // 현재 로그인된 Member == CartItem에 있는 Member -> 같으면 return true
        return true;
    }
    public void updateCartItemCount(Long cartItemId, int count){
        log.info("=========Start========");
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityExistsException::new);
        cartItem.updateCount(count);
    }

    public void deleteCartItem(Long cartItemId){
        log.info("=========Start========");
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityExistsException::new);
        cartItemRepository.delete(cartItem);
    }

    public Long orderCartItem(List<CartOrderDto> cartOrderDtoList, String email){
        log.info("=========Start========");
        // 주문DTO List 객체 생성
        List<OrderDto> orderDtoList = new ArrayList<>();
        // 카트 주문 List에 있는 목록 -> 카트 아이템 객체로 추출
        // 주문 Dto에 CartItem 정보를 담고
        // 위에 선언된 주문 Dto List에 추가
        for(CartOrderDto cartOrderDto : cartOrderDtoList){
            CartItem cartItem = cartItemRepository.findById(cartOrderDto.getCartItemId())
                    .orElseThrow(EntityExistsException::new);
            OrderDto orderDto = new OrderDto();
            orderDto.setItemId(cartItem.getItem().getId());
            orderDto.setCount(cartItem.getCount());
            orderDtoList.add(orderDto);
        }
        // 주문DTO리스트 현재 로그인된 이메일 매개변수 넣고
        // 주문 서비스 실행 -> 주문
        Long orderId = orderService.orders(orderDtoList, email);

        //Cart에서 있던 Item 주문이 되니까 CartItem 모두 삭제
        for(CartOrderDto cartOrderDto : cartOrderDtoList){
            CartItem cartItem = cartItemRepository.findById(cartOrderDto.getCartItemId())
                    .orElseThrow(EntityExistsException::new);
            cartItemRepository.delete(cartItem);
        }
        return orderId;
    }

}
