package habsida.spring.boot_security.demo.configs;

import habsida.spring.boot_security.demo.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private UserServiceImpl userServiceImpl;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(daoAuthenticationProvider())
                .authorizeRequests(auth -> auth
                        .antMatchers(HttpMethod.GET, "/api/admin/**").hasRole("ADMIN")
                        .antMatchers(HttpMethod.POST, "/api/admin/**").hasRole("ADMIN")
                        .antMatchers(HttpMethod.PUT, "/api/admin/**").hasRole("ADMIN")
                        .antMatchers(HttpMethod.DELETE, "/api/admin/**").hasRole("ADMIN")
                        .antMatchers("/admin/**").hasRole("ADMIN")
                        .antMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                        .antMatchers("/api/users/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf
                        .ignoringAntMatchers("/api/**")  // disable CSRF for API calls
                        .disable()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(successUserHandler())
                        .failureHandler(customAuthenticationFailureHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userServiceImpl);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    public WebSecurityConfig(UserServiceImpl userServiceImpl,
                             PasswordEncoder passwordEncoder) {
        this.userServiceImpl = userServiceImpl;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public AuthenticationSuccessHandler successUserHandler() {
        return new SuccessUserHandler();
    }
}
