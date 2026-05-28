package com.guilhermebraga.agendamento_api.config;

import com.guilhermebraga.agendamento_api.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Configuração central de segurança da aplicação.
 *
 * Autenticação suportada:
 *   1. JWT Bearer token  — para a API REST (/api/v1/**)
 *   2. HTTP Basic auth   — alternativa para Swagger UI e testes
 *
 * Regras de autorização da API REST:
 *   GET           → ROLE_USER ou ROLE_ADMIN
 *   POST/PUT/PATCH/DELETE → apenas ROLE_ADMIN
 *
 * Caminhos públicos: dashboard, portal, views web (/web/**), Swagger UI,
 * recursos estáticos e endpoint de login JWT.
 *
 * @author Guilherme Braga
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()))
            .authorizeHttpRequests(auth -> auth

                // --- Páginas públicas, dashboard e handler de erros ---
                .requestMatchers("/", "/dashboard", "/dashboard/**", "/error", "/portal/**", "/web/**").permitAll()

                // --- Recursos estáticos ---
                .requestMatchers(
                    "/css/**", "/js/**", "/images/**", "/static/**", "/favicon.ico"
                ).permitAll()

                // --- Swagger UI e OpenAPI ---
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/api-docs/**",
                    "/v3/api-docs/**"
                ).permitAll()

                // --- H2 Console ---
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/error").permitAll()

                // --- Portal do cliente (sessão própria, sem Spring Security) ---
                .requestMatchers("/portal/**").permitAll()

                // --- Endpoint de autenticação JWT ---
                .requestMatchers("/api/v1/auth/**").permitAll()

                // --- Login do painel admin (a própria página de login é pública) ---
                .requestMatchers("/web/login").permitAll()

                // --- Endpoint de login (JWT) ---
                .requestMatchers("/api/v1/auth/**").permitAll()

                // --- API REST: leitura → USER ou ADMIN ---
                .requestMatchers(HttpMethod.GET, "/api/v1/**").hasAnyRole("USER", "ADMIN")

                // --- API REST: escrita → somente ADMIN ---
                .requestMatchers(HttpMethod.POST,   "/api/v1/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/v1/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH,  "/api/v1/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/**").hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            // JWT filter runs before the Basic auth filter so Bearer tokens are validated first
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .httpBasic(withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        var admin = User.builder()
                .username("admin")
                .password(encoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        var user = User.builder()
                .username("user")
                .password(encoder.encode("user123"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, user);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
