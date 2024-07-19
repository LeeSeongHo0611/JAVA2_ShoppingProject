package com.shop.config;

import com.shop.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity // 웹 보안을 가능하게 한다.
public class SecurityConfig {

    @Autowired
    MemberService memberService;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**","/img/**","/favicon.ico","/error").permitAll()
                        .requestMatchers("/mapApi/**").permitAll()
                        .requestMatchers("/","/members/**","/item/**","/images/**","/noticeBoard/**").permitAll()
                        .requestMatchers("/loadItems").permitAll()
                        .requestMatchers("/search").permitAll()
                        .requestMatchers("/boards/notice").permitAll()
                        .requestMatchers("/boards/newBd/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()

                ).formLogin(formLogin -> formLogin
                        .loginPage("/members/login")
                        .defaultSuccessUrl("/")
                        .usernameParameter("email")
                        .failureUrl("/members/login/error")

                ).logout(logout-> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout"))
                        .logoutSuccessUrl("/")
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        // 이 경로에서 CSRF 보호 비활성화
                        .ignoringRequestMatchers("/admin/item/new",
                                // AJAX 상품 전체 목록 표시 URL CSRF 보호 비활성화 - 모든 상품은 고객 관리자 모두 볼수 있어야 한다.
                                "/",
                                // 유저 관리자 외부인 모두 지도를 볼수 있게 토큰 오픈
                                "mapApi/**",
                                // 베스트 item 테스트를 하기 위한 html 오픈
                                "item/**",
                                // 베스트 item img 테스트를 하기 위한 html 오픈
                                "img/**",
                                // 모든 사람이 볼수 있게 공지사항 오픈
                                "/boards/notice",
                                "/boards/newBd/**"

                                )
                )
                .oauth2Login(oauthLogin -> oauthLogin
                        .defaultSuccessUrl("/")
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                .userService(customOAuth2UserService))
                );

        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint()));

        return http.build();
    }

    @Bean
    public static PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Autowired
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(memberService).passwordEncoder(passwordEncoder());
    }
}
