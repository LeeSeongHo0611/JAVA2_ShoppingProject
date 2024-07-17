package com.shop.config;

import com.shop.dto.SessionUser;
import com.shop.entity.Member;
import com.shop.entity.User;
import com.shop.repository.MemberRepository;
import com.shop.repository.UserRepository;
import com.shop.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HttpSession httpSession;

    // 2024 07 16: 소셜로 가입한 유저를 연동하기 위한 memberService 주입
    @Autowired
    MemberService memberService;

    // db에서 맴버를 조회하기 위한 @AutoWired
    @Autowired
    MemberRepository memberRepository;

    /**
     * OAuth2UserRequest를 사용하여 사용자 정보를 로드하고, 이를 바탕으로 사용자 정보를 업데이트하거나 저장한 후 반환합니다.
     *
     * @param oAuth2UserRequest OAuth2 인증 요청
     * @return OAuth2User 인증된 사용자 정보
     * @throws OAuth2AuthenticationException OAuth2 인증 예외
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        // DefaultOAuth2UserService를 사용하여 OAuth2 사용자 정보를 로드
        OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = oAuth2UserService.loadUser(oAuth2UserRequest);

        // 현재 로그인한 서비스 제공자(Google, Kakao 등)의 ID를 가져옴
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();

        // 사용자 정보에서 사용할 주요 속성의 이름을 가져옴 (예: Google의 경우 sub, Kakao의 경우 id)
        String userNameAttributeName = oAuth2UserRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        // 사용자 정보를 OAuthAttributes 객체로 변환
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        // 사용자 정보를 업데이트하거나 새로운 사용자로 저장
        User user = saveOrUpdate(attributes);

        // 세션에 사용자 정보를 저장
        httpSession.setAttribute("user", new SessionUser(user));

        // 인증된 사용자 정보를 DefaultOAuth2User 객체로 반환
        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes.getAttributes(), attributes.getNameAttributeKey());
    }

    /**
     * 사용자 정보를 업데이트하거나 새로운 사용자로 저장합니다.
     *
     * @param attributes OAuthAttributes 객체
     * @return 저장된 사용자 객체
     */
    private User saveOrUpdate(OAuthAttributes attributes){
        // 사용자 이메일을 통해 기존 사용자 정보를 조회
        User user = userRepository.findByEmail(attributes.getEmail())
                // 기존 사용자가 존재하면 이름과 프로필 사진을 업데이트
                .map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
                // 기존 사용자가 존재하지 않으면 새로운 사용자 엔티티를 생성
                .orElse(attributes.toEntity());

        // 사용자 정보를 저장 또는 업데이트한 후, 저장된 사용자 객체를 반환
        Member existingMember = memberRepository.findByEmail(attributes.getEmail());
        //DB에 입력할 맴버 객체
        Member member = new Member();
        if (existingMember == null){
            //유저 테이블의 이메일과 맴버 테이블의 이메일이 같으면 통과 (이미있음)
            //그허
            member.setName(user.getName());
            member.setEmail(user.getEmail());
            member.setPassword("Oauth2");
            member.setAddress("Oauth2");
            member.setTel("Oauth2");
            memberService.saveMember(member);
            System.out.println("회원가입 완료: " + member);
        }


        // 장바구니 등을 사용하기 위해  User -> Member 객체 변환
        return userRepository.save(user);
    }
}
