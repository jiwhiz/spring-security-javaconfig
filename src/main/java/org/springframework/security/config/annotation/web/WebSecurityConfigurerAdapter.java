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
package org.springframework.security.config.annotation.web;


import java.util.Arrays;

import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.LazyInitTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.AuthenticationBuilder;
import org.springframework.security.config.annotation.authentication.AuthenticationRegistry;
import org.springframework.security.config.annotation.web.SpringSecurityFilterChainBuilder.IgnoredRequestRegistry;
import org.springframework.security.config.authentication.AuthenticationManagerFactoryBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Rob Winch
 *
 */
public abstract class WebSecurityConfigurerAdapter implements WebSecurityConfigurer {
    @Autowired
    private ApplicationContext context;

    private final AuthenticationBuilder authenticationBuilder = new AuthenticationBuilder();
    private AuthenticationBuilder parentAuthenticationRegistry = new AuthenticationBuilder();
    private boolean disableAuthenticationRegistry;
    private boolean authenticationManagerInitialized;
    private AuthenticationManager authenticationManager;
    private HttpConfigurator springSecurityFilterChain;

    protected void registerAuthentication(AuthenticationRegistry registry) throws Exception {
        this.disableAuthenticationRegistry = true;
    }

    protected void applyDefaults(HttpConfigurator http) throws Exception {
        http.applyDefaultConfigurators();
        authorizeUrls(http.authorizeUrls());
    }

    protected abstract void authorizeUrls(ExpressionUrlAuthorizations interceptUrls);

    private HttpConfigurator springSecurityFilterChain() throws Exception {
        if(springSecurityFilterChain == null) {
            AuthenticationManager authenticationManager = authenticationManager();
            authenticationBuilder.parentAuthenticationManager(authenticationManager);
            springSecurityFilterChain = new HttpConfigurator(authenticationBuilder);
        }
        return springSecurityFilterChain;
    }

    public HttpConfigurator httpConfiguration() throws Exception {
        HttpConfigurator springSecurityFilterChain = springSecurityFilterChain();
        springSecurityFilterChain.setSharedObject(UserDetailsService.class, userDetailsService());
        applyDefaults(springSecurityFilterChain);
        configure(springSecurityFilterChain);
        return springSecurityFilterChain;
    }


    @Bean(name=BeanIds.AUTHENTICATION_MANAGER)
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return new AuthenticationManagerDelegator(authenticationBuilder);
    }

    protected AuthenticationManager authenticationManager() throws Exception {
        if(!authenticationManagerInitialized) {
            registerAuthentication(parentAuthenticationRegistry);
            if(disableAuthenticationRegistry) {
                authenticationManager = getBeanExcluding(AuthenticationManager.class, BeanIds.AUTHENTICATION_MANAGER);
            } else {
                authenticationManagerInitialized = true;
                authenticationManager = parentAuthenticationRegistry.build();
            }
            authenticationManagerInitialized = true;
        }
        return authenticationManager;
    }

    @Bean(name=BeanIds.USER_DETAILS_SERVICE)
    public UserDetailsService userDetailsServiceBean() throws Exception {
        return userDetailsService();
    }

    private UserDetailsService userDetailsService() throws Exception {
        return userDetailsService(parentAuthenticationRegistry);
    }

    protected UserDetailsService userDetailsService(AuthenticationRegistry authenticationRegistry) {
        return authenticationRegistry.userDetailsService();
    }

    protected void performConfigure(SpringSecurityFilterChainBuilder securityFilterChains){

    }

    public void init(WebSecurityConfiguration builder) throws Exception {
        SpringSecurityFilterChainBuilder securityFilterChains = builder.springSecurityFilterChainBuilder();
        ignoredRequests(securityFilterChains.ignoring());
        performConfigure(securityFilterChains);
        securityFilterChains
            .securityFilterChains(httpConfiguration());
    }

    public void configure(WebSecurityConfiguration builder) throws Exception {
    }

    protected void ignoredRequests(IgnoredRequestRegistry ignoredRequests) {

    }

    private <T> T getBeanExcluding(Class<T> clazz, String beanNameToExclude) {
        String[] beanNames = context.getBeanNamesForType(clazz);
        if(beanNames.length == 1 && !beanNameToExclude.equals(beanNames[0])) {
            return context.getBean(beanNames[0],clazz);
        }
        if(beanNames.length == 2) {
            if(beanNameToExclude.equals(beanNames[0])) {
                return context.getBean(beanNames[1],clazz);
            }
            if(beanNameToExclude.equals(beanNames[1])) {
                return context.getBean(beanNames[0],clazz);
            }
        }
        return null;
    }

    protected abstract void configure(HttpConfigurator http) throws Exception;

    static final class AuthenticationManagerDelegator implements AuthenticationManager {
        private AuthenticationBuilder delegateBuilder;
        private AuthenticationManager delegate;
        private final Object delegateMonitor = new Object();

        AuthenticationManagerDelegator(AuthenticationBuilder authentication) {
            this.delegateBuilder = authentication;
        }

        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            if(delegate != null) {
                return delegate.authenticate(authentication);
            }

            synchronized(delegateMonitor) {
                if (delegate == null) {
                    delegate = this.delegateBuilder.getObject();
                    this.delegateBuilder = null;
                }
            }

            return delegate.authenticate(authentication);
        }
    }
}