package com.revature.spring_starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(
        // Turned off so Boot stops creating a default in-memory "user" account and
        // printing "Using generated security password: ..." at every startup. That
        // account is unreachable here anyway - we disabled formLogin and httpBasic,
        // and our identities come from the JWT cookie, not a UserDetailsService.
        exclude = UserDetailsServiceAutoConfiguration.class,
        scanBasePackages = {
        "com.revature.spring_starter.controllers",
        "com.revature.spring_starter.services",
        "com.revature.spring_starter.repositories",
        "com.revature.spring_starter.security"
        })
public class SpringStarterApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringStarterApplication.class, args);
	}

}
