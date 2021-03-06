package org.springframework.security.oauth.examples.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.AuthenticationRegistry;
import org.springframework.security.config.annotation.web.EnableWebSecurity;
import org.springframework.security.config.annotation.web.ExpressionUrlAuthorizations;
import org.springframework.security.config.annotation.web.HttpConfigurator;
import org.springframework.security.config.annotation.web.SpringSecurityFilterChainBuilder.IgnoredRequestRegistry;
import org.springframework.security.config.annotation.web.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.web.access.ExceptionTranslationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private OAuth2ClientContextFilter oauth2ClientFilter;

    @Override
    protected void registerAuthentication(
            AuthenticationRegistry builder) throws Exception {
        builder
            .inMemoryAuthentication()
                .withUser("marissa").password("wombat").roles("USER").and()
                .withUser("sam").password("kangaroo").roles("USER");
    }

    protected void authorizeUrls(
            ExpressionUrlAuthorizations interceptUrls) {
        interceptUrls
            .antMatchers("/sparklr/**","/facebook/**").hasRole("USER")
            .antMatchers("/**");
    }

    protected void ignoredRequests(IgnoredRequestRegistry ignoredRequests) {
        ignoredRequests
            .antMatchers("/resources/**");
    }

    protected void configure(
            HttpConfigurator http) throws Exception {
        http
            .addFilterAfter(oauth2ClientFilter, ExceptionTranslationFilter.class)
            .logout()
                .logoutSuccessUrl("/login.jsp")
                .logoutUrl("/logout.do")
                .permitAll()
                .and()
            .formLogin()
                .loginPage("/login.jsp")
                .loginProcessingUrl("/login.do")
                .failureUrl("/login.jsp?authentication_error=true")
                .usernameParameter("j_username")
                .passwordParameter("j_password")
                .permitAll();
    }
}
