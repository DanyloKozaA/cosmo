package com.example.roller.config.configRoller;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()  // Разрешить доступ ко всем запросам без аутентификации
                )
                .csrf(csrf -> csrf.disable())  // Отключить CSRF-защиту
                .formLogin(formLogin -> formLogin.disable())  // Отключить форму входа
                .httpBasic(httpBasic -> httpBasic.disable());  // Отключить базовую аутентификацию

        return http.build();
    }
}