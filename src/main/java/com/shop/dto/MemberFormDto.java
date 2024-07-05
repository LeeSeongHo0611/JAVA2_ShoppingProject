package com.shop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class MemberFormDto {
    @NotBlank(message = "이름은 필수 입력 값입니다.")
    @Pattern(regexp = "^[a-zA-Z가-힣]*$", message = "이름에는 특수문자나 숫자를 포함할 수 없습니다.")
    private String name;

    @NotEmpty(message = "이메일은 필수 입력 값입니다.")
    @Email
    private String email;

    @NotEmpty(message = "비밀번호은 필수 입력 값입니다.")
    @Length(min = 8, max = 16, message = "비밀번호는 8자이상, 16자 이하로 입력해주세요.")
    @Pattern(regexp = "^(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,16}$", message = "비밀번호는 8자 이상, 16자 이하로 입력해주세요. 또한, 적어도 하나의 특수문자를 포함해야 합니다.")
    private String password;

    @NotEmpty(message = "주소는 필수 입력 값입니다.")
    private String address;

    @NotBlank(message = "전화번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^[0-9]{9,11}$", message = "전화번호는 9~11자리의 숫자만 입력 가능합니다.")
    private String tel;
}
