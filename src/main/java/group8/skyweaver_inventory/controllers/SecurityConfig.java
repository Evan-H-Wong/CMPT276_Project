package group8.skyweaver_inventory.controllers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails manager = User.builder()
                .username("manager")
                .password(passwordEncoder.encode("password"))
                .roles("MANAGER")
                .build();

        UserDetails employee = User.builder()
                .username("employee")
                .password(passwordEncoder.encode("password"))
                .roles("EMPLOYEE")
                .build();

        return new InMemoryUserDetailsManager(manager, employee);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/auth/login.html", "/auth/register.html").permitAll()
                        .requestMatchers("/personalized/manager.html").hasRole("MANAGER")
                        .requestMatchers("/personalized/employee.html").hasRole("EMPLOYEE")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login.html")
                        .defaultSuccessUrl("/default")
                        .permitAll()
                )
                .logout(logout -> logout
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .logoutSuccessUrl("/auth/login.html")
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/auth/error.html")
                );
        return http.build();
    }
}
