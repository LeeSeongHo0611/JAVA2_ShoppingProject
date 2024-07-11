package com.shop.dto;
//결제하는데 있어 사용하는 PayDto
//PayDto만든 이유 -> JSON의 정렬화 과정에서 StackOverFlow가 발생하여 무한궤도에 빠지게 되어 DTO로 1:1 맵핑을 도움

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL) // JSON으로 바이트스트림으로 보내기  -> 객체를 JSON형태로 변환 (TEXT)
public class PayDto {
    private String merchant_uid;
    private String payName;
    private String PayAmount;
    private String buyerEmail;
    private String buyerName;
    private String byerTel;
    private String buyerAddr;
    private String buyerPostcode = "TEST";
}
