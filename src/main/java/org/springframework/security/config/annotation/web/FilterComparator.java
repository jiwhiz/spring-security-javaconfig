/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.security.config.annotation.web;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;

import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.authentication.www.DigestAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.jaasapi.JaasApiIntegrationFilter;
import org.springframework.security.web.savedrequest.RequestCacheAwareFilter;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.security.web.session.SessionManagementFilter;

/**
 *
 * @author Rob Winch
 * @since 3.2
 */

class FilterComparator implements Comparator<Filter>{
    private static int STEP = 100;
    private Map<Class<? extends Filter>,Integer> filterToOrder = new HashMap<Class<? extends Filter>,Integer>();

    public FilterComparator() {
        int order = 100;
        filterToOrder.put(ChannelProcessingFilter.class, order);
        order += STEP;
        filterToOrder.put(ConcurrentSessionFilter.class, order);
        order += STEP;
        filterToOrder.put(SecurityContextPersistenceFilter.class, order);
        order += STEP;
        filterToOrder.put(LogoutFilter.class, order);
        order += STEP;
        filterToOrder.put(X509AuthenticationFilter.class, order);
        order += STEP;
        filterToOrder.put(AbstractPreAuthenticatedProcessingFilter.class, order);
        order += STEP;
//        filterToOrder.put(CasFilter, order);
//        order += STEP;
        filterToOrder.put(UsernamePasswordAuthenticationFilter.class, order);
        order += STEP;
        filterToOrder.put(ConcurrentSessionFilter.class, order);
        order += STEP;
//        filterToOrder.put(OpenIDFilter.class, order);
//        order += STEP;
//        filterToOrder.put(LoginPage.class, order);
//        order += STEP;
        filterToOrder.put(ConcurrentSessionFilter.class, order);
        order += STEP;
        filterToOrder.put(DigestAuthenticationFilter.class, order);
        order += STEP;
        filterToOrder.put(BasicAuthenticationFilter.class, order);
        order += STEP;
        filterToOrder.put(RequestCacheAwareFilter.class, order);
        order += STEP;
        filterToOrder.put(SecurityContextHolderAwareRequestFilter.class, order);
        order += STEP;
        filterToOrder.put(JaasApiIntegrationFilter.class, order);
        order += STEP;
        filterToOrder.put(RememberMeAuthenticationFilter.class, order);
        order += STEP;
        filterToOrder.put(AnonymousAuthenticationFilter.class, order);
        order += STEP;
        filterToOrder.put(SessionManagementFilter.class, order);
        order += STEP;
        filterToOrder.put(ExceptionTranslationFilter.class, order);
        order += STEP;
        filterToOrder.put(FilterSecurityInterceptor.class, order);
        order += STEP;
        filterToOrder.put(SwitchUserFilter.class, order);
    }

    public int compare(Filter lhs, Filter rhs) {
        Integer left = getOrder(lhs.getClass());
        Integer right = getOrder(rhs.getClass());
        return left - right;
    }

    public boolean registered(Class<? extends Filter> filter) {
        return getOrder(filter) != null;
    }

    public void registerAfter(Class<? extends Filter> filter, Class<? extends Filter> afterFilter) {
        Integer position = getOrder(afterFilter);
        if(position == null) {
            throw new IllegalArgumentException("Cannot register after un registered Filter "+afterFilter);
        }

        filterToOrder.put(filter, position + 1);
    }

    public void registerBefore(Class<? extends Filter> filter, Class<? extends Filter> afterFilter) {
        Integer position = getOrder(afterFilter);
        if(position == null) {
            throw new IllegalArgumentException("Cannot register after un registered Filter "+afterFilter);
        }

        filterToOrder.put(filter, position - 1);
    }

    private Integer getOrder(Class<?> clazz) {
        while(clazz != null) {
            Integer result = filterToOrder.get(clazz);
            if(result != null) {
                return result;
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }
}
