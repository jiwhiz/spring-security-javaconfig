/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.security.config.annotation.issue50;

import org.spockframework.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.config.annotation.authentication.AuthenticationRegistry;
import org.springframework.security.config.annotation.issue50.domain.User;
import org.springframework.security.config.annotation.issue50.repo.UserRepository;
import org.springframework.security.config.annotation.method.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.EnableWebSecurity;
import org.springframework.security.config.annotation.web.ExpressionUrlAuthorizations;
import org.springframework.security.config.annotation.web.HttpConfigurator;
import org.springframework.security.config.annotation.web.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author Rob Winch
 *
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private UserRepository myUserRepository;

    protected void registerAuthentication(
            AuthenticationRegistry builder) throws Exception {
        builder
                .add(authenticationProvider());
    }

    protected void authorizeUrls(ExpressionUrlAuthorizations interceptUrls) {
        interceptUrls
            .antMatchers("/**").permitAll();
    }

    protected void configure(HttpConfigurator http) throws Exception {
        http
            .applyDefaultConfigurators();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        Assert.notNull(myUserRepository);
        return new AuthenticationProvider() {
            public boolean supports(Class<?> authentication) {
                return true;
            }
            public Authentication authenticate(Authentication authentication)
                    throws AuthenticationException {
                Object principal = authentication.getPrincipal();
                String username = String.valueOf(principal);
                User user = myUserRepository.findByUsername(username);
                if(user == null) {
                    throw new UsernameNotFoundException("No user for principal "+principal);
                }
                if(!authentication.getCredentials().equals(user.getPassword())) {
                    throw new BadCredentialsException("Invalid password");
                }
                return new TestingAuthenticationToken(principal, null, "ROLE_USER");
            }
        };
    }
}
