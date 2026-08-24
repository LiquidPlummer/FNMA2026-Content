package com.revature.spring_starter.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * The whole security setup lives here: which beans exist, where our filter slots
 * into the chain, and which roles may reach which URLs.
 *
 * Request flow once this is in place:
 *
 *   Request -> FilterChainProxy
 *                |- CorsFilter                       answers preflight, adds CORS headers
 *                |- SecurityContextHolderFilter       installs an EMPTY context
 *                |- JwtCookieAuthenticationFilter     OURS: cookie -> verify -> set context
 *                |- AnonymousAuthenticationFilter     fills a still-empty context with "anonymousUser"
 *                |- ExceptionTranslationFilter        catches what AuthorizationFilter throws
 *                |- AuthorizationFilter               applies the authorizeHttpRequests rules
 *            -> DispatcherServlet -> Controller
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public JwtService jwtService(@Value("${app.jwt.secret}") String secret,
                                 @Value("${app.jwt.ttl-seconds:1800}") long ttlSeconds) {
        return new JwtService(secret, ttlSeconds);
    }

    /**
     * BCrypt hashes are salted and deliberately slow, which is what you want for
     * passwords. Note encode() returns a different string every call, so you check
     * a password with matches(raw, hash), never by hashing and comparing strings.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtService jwtService) throws Exception {

        // Built here rather than declared as a @Bean on purpose. Spring Boot
        // auto-registers any Filter bean with the servlet container as well,
        // which would run it a second time outside this chain for every request.
        JwtCookieAuthenticationFilter jwtFilter = new JwtCookieAuthenticationFilter(jwtService);

        return http
                // Picks up the CorsConfigurationSource bean defined below.
                .cors(Customizer.withDefaults())

                // Spring Security turns these on by default and we use none of them.
                // CSRF: see the note at the bottom of this file. That one is a real
                // trade-off, not just boilerplate.
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)

                // No HttpSession, ever. Identity is rebuilt from the token on
                // every single request, which is what makes this scale sideways.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Slot our filter in BEFORE UsernamePasswordAuthenticationFilter.
                // That position matters: it puts us ahead of
                // AnonymousAuthenticationFilter, so getAuthentication() is still
                // null when we look. Registering against AuthorizationFilter.class
                // instead would place us AFTER the anonymous token was installed,
                // our null check would never pass, and nobody would authenticate.
                // (Ordering uses a static registry of filter positions, so this
                // works even though formLogin is disabled and that filter is not
                // actually in the chain.)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                // Rules are evaluated top to bottom and FIRST MATCH WINS, so the
                // order of these lines is the policy. Specific before general.
                .authorizeHttpRequests(auth -> auth
                        // Anyone may register or log in. You cannot require a
                        // token on the endpoint whose job is handing out tokens.
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()

                        // Not optional, and the cause of a genuinely baffling bug
                        // if you leave it out. response.sendError() does not write
                        // the response directly - it asks the container to run an
                        // ERROR dispatch to /error, which re-enters this filter
                        // chain. By then the SecurityContext has been cleared, so
                        // /error is evaluated as anonymous, anyRequest() denies it,
                        // and the 403 you meant to send gets overwritten with a
                        // 401. Symptom: wrong-role users get 401 instead of 403.
                        .requestMatchers("/error").permitAll()

                        // Any logged-in user, either role.
                        .requestMatchers("/api/me").authenticated()

                        // ADMIN only. hasRole("ADMIN") checks for the authority
                        // "ROLE_ADMIN" - the prefix is added for you here.
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // You can also vary the rule by HTTP METHOD on the same
                        // URL, which is how read-vs-delete usually gets split:
                        //
                        //   .requestMatchers(HttpMethod.GET,    "/api/notes/**").hasAnyRole("USER", "ADMIN")
                        //   .requestMatchers(HttpMethod.DELETE, "/api/notes/**").hasRole("ADMIN")

                        // Deny by default. Anything not named above needs a login.
                        // KEEP THIS LAST - it matches everything.
                        .anyRequest().authenticated())

                // Without this, an unauthenticated call gets redirected to a login
                // page that does not exist. An API should just say 401.
                .exceptionHandling(ex -> ex
                        // Not logged in at all -> 401 Unauthorized, "who are you?"
                        .authenticationEntryPoint((request, response, authEx) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                        // Logged in, wrong role -> 403 Forbidden, "I know you, and no."
                        .accessDeniedHandler((request, response, deniedEx) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN)))

                .build();
    }

    /**
     * CORS. The browser blocks cross-origin reads unless the server opts in with
     * these headers, and this bean is that opt-in.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        // The important line for cookie auth. Without it the browser refuses to
        // attach our cookie to cross-origin requests, and a front-end team spends
        // an afternoon wondering why login "worked" but every call after it 401s.
        // The front end has to opt in on its side too, with
        // fetch(url, { credentials: "include" }).
        //
        // This is also exactly why setAllowedOrigins cannot be "*" here. Spring
        // throws at startup if you try to combine the two, because "send my
        // cookies to anybody who asks" is the hole CORS exists to close.
        config.setAllowCredentials(true);

        // How long the browser may cache the preflight OPTIONS response.
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

/*
 * OPEN ITEM - CSRF is disabled above, and with cookie auth that is a genuine
 * hole rather than a formality. The browser attaches cookies to requests your
 * site did not make, so another origin can trigger a state-changing call as your
 * logged-in user. Bearer tokens in an Authorization header dodge this, which is
 * why so much JWT advice says "just disable CSRF" - that advice does not carry
 * over to cookies. SameSite=Lax on the cookie blocks the common cross-site cases
 * and is what we lean on here. Before anything real, turn CSRF back on with
 * CookieCsrfTokenRepository.withHttpOnlyFalse().
 */
