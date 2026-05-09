package com.cloudmall.framework.config;

import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.boot.autoconfigure.elasticsearch.RestClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/3/5
 */
@Configuration
public class ElasticsearchKeepaliveConfig {
    private static final long TCP_KEEPALIVE_TIME = 30L;

    @Bean
    public RestClientBuilderCustomizer restClientBuilderCustomizer() {
        return new RestClientBuilderCustomizer() {
            @Override
            public void customize(RestClientBuilder builder) {
            }

            @Override
            public void customize(HttpAsyncClientBuilder builder) {
                builder.setKeepAliveStrategy(
                        (response, context) -> TCP_KEEPALIVE_TIME
                );
            }
        };
    }
}
