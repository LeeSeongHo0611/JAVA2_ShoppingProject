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
@EnableWebSecurity //웹 보안을 가능하게 한다.
public class SecurityConfig {

    @Autowired
    MemberService memberService;

//    @Autowired
//    private CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**","/img/**","/favicon.ico","/error").permitAll()
                .requestMatchers("/","/members/**","/item/**","/images/**").permitAll()
                .requestMatchers("/loadItems").permitAll()
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

        ).csrf(csrf -> csrf
                        .ignoringRequestMatchers("/admin/item/new") // 이 경로에서 CSRF 보호 비활성화


        ).csrf(csrf -> csrf
                        .ignoringRequestMatchers("/") // AJAX 상품 전체 목록 표시 URL CSRF 보호 비활성화 - 모든 상품은 고객 관리자 모두 볼수 있어야 한다.


//        ).oauth2Login(oauthLogin -> oauthLogin
//                .defaultSuccessUrl("/")
//                .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
//                .userService(customOAuth2UserService))
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
