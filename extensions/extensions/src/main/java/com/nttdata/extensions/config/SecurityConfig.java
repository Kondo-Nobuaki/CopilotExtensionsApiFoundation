package com.nttdata.extensions.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;

import com.nttdata.extensions.config.oauth2.handler.CustomLogoutHandler;
import com.nttdata.extensions.config.oauth2.handler.CustomLogoutSuccessHandler;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomLogoutSuccessHandler customLogoutSuccessHandler;

    @Autowired
    private CustomLogoutHandler customLogoutHandler;

    @Bean
    public DefaultSecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf((csrf) -> csrf.disable());
		http.authorizeHttpRequests((requests) -> requests
				.requestMatchers("/api/admin/**").permitAll()
				.requestMatchers("/loginPage", "/loginPage.html", "/oauth2/**")
				.permitAll()
				.anyRequest().authenticated())
				// ここで認可も実施したい。組織内ユーザーであるか確認したい。
				.oauth2Login((oauth2) -> oauth2
						.loginPage("/loginPage")
						.defaultSuccessUrl("/index", true)
                )
				.logout((logout) -> logout
						.logoutUrl("/logout")
						.addLogoutHandler(customLogoutHandler)
						.logoutSuccessHandler(customLogoutSuccessHandler)
						.permitAll()
						.clearAuthentication(true)
						.invalidateHttpSession(true)
						.deleteCookies("JSESSIONID"));

		return http.build();
	}
}
