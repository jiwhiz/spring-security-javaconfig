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

import org.springframework.security.config.annotation.AbstractSecurityConfigurator;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * @author Rob Winch
 *
 */
public class SecurityContextConfigurator extends AbstractSecurityConfigurator<DefaultSecurityFilterChain,HttpConfigurator> {

    protected void doConfigure(HttpConfigurator http) throws Exception {

        SecurityContextPersistenceFilter securityContextFilter = new SecurityContextPersistenceFilter(http.getSharedObject(SecurityContextRepository.class));
        SessionManagementConfigurator sessionManagement = http.getConfigurator(SessionManagementConfigurator.class);
        SessionCreationPolicy sessionCreationPolicy = sessionManagement.sessionCreationPolicy();
        if(SessionCreationPolicy.always == sessionCreationPolicy) {
            securityContextFilter.setForceEagerSessionCreation(true);
        }
        securityContextFilter.afterPropertiesSet();
        http.addFilter(securityContextFilter);
    }
}