package com.uranus.taskmanager.api.global.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.uranus.taskmanager.api.security.authentication.interceptor.AuthenticationInterceptor;
import com.uranus.taskmanager.api.security.authentication.resolver.LoginMemberArgumentResolver;
import com.uranus.taskmanager.api.security.authorization.interceptor.AuthorizationInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

	private final AuthorizationInterceptor authorizationInterceptor;
	private final AuthenticationInterceptor authenticationInterceptor;
	private final LoginMemberArgumentResolver loginMemberArgumentResolver;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(authenticationInterceptor);
		registry.addInterceptor(authorizationInterceptor);
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(loginMemberArgumentResolver);
	}
}
