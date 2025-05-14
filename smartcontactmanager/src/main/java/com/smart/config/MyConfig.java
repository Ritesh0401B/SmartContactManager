package com.smart.config;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;

@Configuration
public class MyConfig {

	    @Bean
	    public UserDetailsService userDetailsService() {
	        return new UserDetailsServiceImpl();   
	    }

	    @Bean
	    public BCryptPasswordEncoder passwordEncoder() {
	        return new BCryptPasswordEncoder();
	    }

	    @Bean
	    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
	        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
	        authProvider.setUserDetailsService(userDetailsService);
	        authProvider.setPasswordEncoder(passwordEncoder());
	        return authProvider;
	    }
	    
	    
	    @Bean
	    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
	        return config.getAuthenticationManager();
	    }


//	    @Bean
//	    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//	        http
//	            .csrf(csrf -> csrf.disable())
//	            .authorizeHttpRequests(auth -> auth
//	                    .requestMatchers("/", "/about", "/signup", "/do_register", "/css/**", "/js/**", "/image/**")
//	                    .permitAll()
//	                    .requestMatchers("/user/**").hasRole("USER")
//	                    .requestMatchers("/admin/**").hasRole("ADMIN")
//	            )
//	            .formLogin(form -> form
//	                    .loginPage("/signin")
//	                    .loginProcessingUrl("/do_login")
//	                    .defaultSuccessUrl("/user/index")
//	                    .permitAll()
//	            )
//	            .logout(logout -> logout
//	                    .logoutUrl("/logout")
//	                    .logoutSuccessUrl("/")
//	            );
//
//	        return http.build();
//	    }
	    
	    @Bean
	    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

	    	http
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers("/user/**").hasRole("USER")
	            .requestMatchers("/admin/**").hasRole("ADMIN")
	            .anyRequest().permitAll()
	        )
	        .formLogin(form -> form
	            .loginPage("/signin")
	            .loginProcessingUrl("/dologin")  // same as form action
	            .defaultSuccessUrl("/user/index")  // ✅ yeh add karo
	            .failureUrl("/signin?error=true")
	            .permitAll()
	        )
	        .rememberMe(remember -> remember
	        		.key("riteshSmartContact@2025")  // secret key 
	    	        .tokenValiditySeconds(7 * 24 * 60 * 60)  // 7 days
	    	        .rememberMeParameter("remember-me")  // form field name (checkbox)
	    	)
	        .logout(logout -> logout
	                .logoutUrl("/logout")              // default hi hai
	                .logoutSuccessUrl("/signin?logout") // logout hone ke baad redirect
	                .permitAll()
	            )
	            .csrf(csrf -> csrf.disable());
	    	
	        return http.build();
	    }
	    
	    
	    
	// configure method

}
