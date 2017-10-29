/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-present IxorTalk CVBA
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.ixortalk.aws.cognito.boot.config;

import com.ixortalk.aws.cognito.boot.filter.AwsCognitoIdTokenProcessor;
import com.ixortalk.aws.cognito.boot.filter.AwsCognitoJwtAuthenticationFilter;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

import java.net.MalformedURLException;
import java.net.URL;

import static com.nimbusds.jose.JWSAlgorithm.RS256;

@Configuration
@ConditionalOnClass({AwsCognitoJwtAuthenticationFilter.class, AwsCognitoIdTokenProcessor.class})
@EnableConfigurationProperties({AwsCognitoJwtConfiguration.class})
public class AwsCognitoAutoConfiguration {

    private static final Log logger = LogFactory.getLog(AwsCognitoAutoConfiguration.class);

    @Bean
    @Scope(value="request", proxyMode= ScopedProxyMode.TARGET_CLASS)
    public AwsCognitoCredentialsHolder awsCognitoCredentialsHolder() {
        return new AwsCognitoCredentialsHolder();
    }

    @Bean
    public AwsCognitoIdTokenProcessor awsCognitoIdTokenProcessor() { return new AwsCognitoIdTokenProcessor(); }

    @Bean
    public JwtAuthenticationProvider jwtAuthenticationProvider() { return new JwtAuthenticationProvider(); }


    @Bean
    public AwsCognitoJwtAuthenticationFilter awsCognitoJwtAuthenticationFilter() {
        return new AwsCognitoJwtAuthenticationFilter(awsCognitoIdTokenProcessor());
    }

    @Autowired
    private AwsCognitoJwtConfiguration awsCognitoJtwConfiguration;

    @Bean
    public ConfigurableJWTProcessor cognitoJwtProcessor() throws MalformedURLException {
    	logger.debug("Configuring "+AwsCognitoJwtConfiguration.class.getName()+" as ConfigurableJWTProcessor");
        ResourceRetriever resourceRetriever = new DefaultResourceRetriever(awsCognitoJtwConfiguration.getConnectionTimeout(), awsCognitoJtwConfiguration.getReadTimeout());
        URL jwkSetURL = new URL(awsCognitoJtwConfiguration.getJwkUrl());
        JWKSource keySource = new RemoteJWKSet(jwkSetURL, resourceRetriever);
        ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();
        JWSKeySelector keySelector = new JWSVerificationKeySelector(RS256, keySource);
        jwtProcessor.setJWSKeySelector(keySelector);
        return jwtProcessor;
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public JwtIdTokenCredentialsHolder jwtidTokenCredentialsholder() {
    	return new JwtIdTokenCredentialsHolder();
    }
}