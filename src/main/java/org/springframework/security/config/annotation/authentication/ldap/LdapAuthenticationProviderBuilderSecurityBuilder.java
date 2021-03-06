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
package org.springframework.security.config.annotation.authentication.ldap;

import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.AbstractSecurityConfigurator;
import org.springframework.security.config.annotation.SecurityBuilder;
import org.springframework.security.config.annotation.authentication.AuthenticationBuilder;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

/**
 *
 * @author Rob Winch
 * @since 3.2
 */
public class LdapAuthenticationProviderBuilderSecurityBuilder extends AbstractSecurityConfigurator<AuthenticationManager,AuthenticationBuilder> implements
        SecurityBuilder<LdapAuthenticationProvider>, LdapAuthenticationRegistry {
    private String groupRoleAttribute = "cn";
    private String groupSearchBase = "ou=groups";
    private String groupSearchFilter = "(uniqueMember={0})";
    private String rolePrefix = "ROLE_";
    private String userSearchBase = ""; // only for search
    private String userSearchFilter = null;//"uid={0}"; // only for search
    private String[] userDnPatterns;
    private BaseLdapPathContextSource contextSource;
    private UserDetailsContextMapper userDetailsContextMapper;

    public LdapAuthenticationProvider build() throws Exception {
        BaseLdapPathContextSource contextSource = getContextSource();
        BindAuthenticator ldapAuthenticator = new BindAuthenticator(
                contextSource);
        if(userDnPatterns != null && userDnPatterns.length > 0) {
            ldapAuthenticator.setUserDnPatterns(userDnPatterns);
        }
        if(userSearchFilter != null) {
            FilterBasedLdapUserSearch userSearch = new FilterBasedLdapUserSearch(
                    groupSearchBase, userSearchFilter, contextSource);
            ldapAuthenticator.setUserSearch(userSearch);
        }

        DefaultLdapAuthoritiesPopulator authoritiesPopulator = new DefaultLdapAuthoritiesPopulator(
                contextSource, groupSearchBase);
        authoritiesPopulator.setGroupRoleAttribute(groupRoleAttribute);
        authoritiesPopulator.setGroupSearchFilter(groupSearchFilter);

        LdapAuthenticationProvider ldapAuthenticationProvider = new LdapAuthenticationProvider(
                ldapAuthenticator, authoritiesPopulator);
        SimpleAuthorityMapper simpleAuthorityMapper = new SimpleAuthorityMapper();
        simpleAuthorityMapper.setPrefix(rolePrefix);
        simpleAuthorityMapper.afterPropertiesSet();
        ldapAuthenticationProvider.setAuthoritiesMapper(simpleAuthorityMapper);
        if(userDetailsContextMapper != null) {
            ldapAuthenticationProvider.setUserDetailsContextMapper(userDetailsContextMapper);
        }
        return ldapAuthenticationProvider;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.security.config.annotation.authentication.ldap.LdapAuthenticationRegistry#contextSource(org.springframework.ldap.core.support.BaseLdapPathContextSource)
     */
    public LdapAuthenticationProviderBuilderSecurityBuilder contextSource(BaseLdapPathContextSource contextSource) {
        this.contextSource = contextSource;
        return this;
    }

    public LdapAuthenticationProviderBuilderSecurityBuilder userDnPatterns(String...userDnPatterns) {
        this.userDnPatterns = userDnPatterns;
        return this;
    }

    public LdapAuthenticationProviderBuilderSecurityBuilder userDetailsContextMapper(UserDetailsContextMapper userDetailsContextMapper) {
        this.userDetailsContextMapper = userDetailsContextMapper;
        return this;
    }

    public LdapAuthenticationProviderBuilderSecurityBuilder groupRoleAttribute(String groupRoleAttribute) {
        this.groupRoleAttribute = groupRoleAttribute;
        return this;
    }

    public LdapAuthenticationProviderBuilderSecurityBuilder groupSearchBase(String groupSearchBase) {
        this.groupSearchBase = groupSearchBase;
        return this;
    }

    public LdapAuthenticationProviderBuilderSecurityBuilder groupSearchFilter(String groupSearchFilter) {
        this.groupSearchFilter = groupSearchFilter;
        return this;
    }

    public LdapAuthenticationProviderBuilderSecurityBuilder rolePrefix(String rolePrefix) {
        this.rolePrefix = rolePrefix;
        return this;
    }

    public LdapAuthenticationProviderBuilderSecurityBuilder userSearchFilter(String userSearchFilter) {
        this.userSearchFilter = userSearchFilter;
        return this;
    }

    protected void doConfigure(AuthenticationBuilder builder) throws Exception {
        builder.add(build());
    }

    private BaseLdapPathContextSource getContextSource() {
        return contextSource;
    }
}