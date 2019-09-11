package org.humancellatlas.ingest.security;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.spring.security.api.BearerSecurityContextRepository;
import com.auth0.spring.security.api.JwtAuthenticationEntryPoint;
import com.auth0.spring.security.api.JwtAuthenticationProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration
        .WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpMethod.*;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String FORWARDED_FOR = "X-Forwarded-For";

    @Value(value = "${USR_AUTH_AUDIENCE:https://dev.data.humancellatlas.org/}")
    private String audience;
    @Value(value = "${AUTH_ISSUER:https://humancellatlas.auth0.com/}")
    private String issuer;

    @Value(value = "${SVC_AUTH_AUDIENCE:https://dev.data.humancellatlas.org/}")
    private String serviceAudience;

    @Value(value= "${GCP_PROJECT_WHITELIST:hca-dcp-production.iam.gserviceaccount.com,human-cell-atlas-travis-test.iam.gserviceaccount.com,broad-dsde-mint-dev.iam.gserviceaccount.com,broad-dsde-mint-test.iam.gserviceaccount.com,broad-dsde-mint-staging.iam.gserviceaccount.com}")
    private String projectWhitelist;

    private static final List<AntPathRequestMatcher> SECURED_ANT_PATHS;
    static {
        List<AntPathRequestMatcher> antPathMatchers = new ArrayList<>();
        antPathMatchers.addAll(defineAntPathMatchers(GET, "/user/**"));
        antPathMatchers.addAll(defineAntPathMatchers(PATCH, "/**"));
        antPathMatchers.addAll(defineAntPathMatchers(PUT, "/**"));
        antPathMatchers.addAll(defineAntPathMatchers(POST,"/messaging/**", "/submissionEnvelopes/*/projects", "/files**", "/biomaterials**", "/protocols**", "/processes**", "/files**", "/bundleManifests**"));
        SECURED_ANT_PATHS = Collections.unmodifiableList(antPathMatchers);
    }

    private static List<AntPathRequestMatcher> defineAntPathMatchers(HttpMethod method,
            String...patterns) {
        return Stream.of(patterns)
                .map(pattern -> new AntPathRequestMatcher(pattern, method.name()))
                .collect(toList());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        List<String> projectWhitelist = asList(this.projectWhitelist.split(","));
        GoogleServiceJwtAuthenticationProvider googleServiceJwtAuthenticationProvider =
                new GoogleServiceJwtAuthenticationProvider(serviceAudience, projectWhitelist);

        JwkProvider jwkProvider = new JwkProviderBuilder(issuer).build();
        JwtAuthenticationProvider auth0Provider = new JwtAuthenticationProvider(jwkProvider, issuer, audience);

        http.authenticationProvider(auth0Provider)
                .authenticationProvider(googleServiceJwtAuthenticationProvider)
                .securityContext().securityContextRepository(new BearerSecurityContextRepository())
                .and()
                .exceptionHandling().authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                .and()
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .cors().and()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/submissionEnvelopes").authenticated()
                .antMatchers(HttpMethod.POST, "/projects**").authenticated()
                .requestMatchers(this::isRequestForSecuredResourceFromProxy).authenticated()
                .antMatchers(GET, "/**").permitAll();
    }

    private Boolean isRequestForSecuredResourceFromProxy(HttpServletRequest request) {
        return SECURED_ANT_PATHS.stream().anyMatch(matcher -> matcher.matches(request)) &&
                Optional.ofNullable(request.getHeader(FORWARDED_FOR)).isPresent();
    }

}