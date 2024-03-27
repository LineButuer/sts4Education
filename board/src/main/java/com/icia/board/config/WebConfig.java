package com.icia.board.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.icia.board.util.SessionIntercepter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	// 세션 인터셉트 클래스를 자동으로 등록
	@Autowired
	private SessionIntercepter intercepter;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(intercepter) // 인터셉터 객체 등록
				.addPathPatterns("/**") // 인터셉터 범위
				.excludePathPatterns("/", "/css/**","/js/**","/images/**") // 인터셉터 제외 url
				.excludePathPatterns("/joinForm", "/loginForm", "/idCheck")
				.excludePathPatterns("/joinProc", "/loginProc","/error/**")
				.excludePathPatterns("/authUser", "/mailconfirm","/codeAuth")
				.excludePathPatterns("/pwdChange", "/pwdChangeProc");
		
	}
	

}
