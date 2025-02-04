package com.ecommerce.webapp.ecommerce.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Autowired
    private AuthSucessHandlerImpl authSucessHandler;
    @Autowired
    @Lazy
    private AuthFailureHandlerImpl authFailureHandler;

    @Bean
    public PasswordEncoder passwordEncoder () {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService () {
        return new UserDetailsServiceImpl();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider () {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());

        return daoAuthenticationProvider;
    }

    @Bean
    public SecurityFilterChain filterChain (HttpSecurity http)  throws  Exception{

        http.csrf(csrf->csrf.disable()).cors(cors->cors.disable())
                .authorizeHttpRequests(req->req.requestMatchers("/user/**").hasRole("USER")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/**").permitAll())
                .formLogin(form->form.loginPage("/signin")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/")
                        .failureHandler(authFailureHandler)
                        .successHandler(authSucessHandler))
                .logout(logout->logout.permitAll());

        return http.build();
    }
}
