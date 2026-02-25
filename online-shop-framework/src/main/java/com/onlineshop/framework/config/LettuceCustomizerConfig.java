package com.onlineshop.framework.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.SocketOptions.KeepAliveOptions;
import io.lettuce.core.SocketOptions.TcpUserTimeoutOptions;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/3/5
 */
@Configuration
public class LettuceCustomizerConfig {
    private static final int TCP_KEEPALIVE_IDLE = 30;
    private static final int TCP_USER_TIMEOUT = 30;

    @Bean
    public LettuceClientConfigurationBuilderCustomizer customizer() {
        KeepAliveOptions keepAliveOptions = KeepAliveOptions.builder()
                                                            .enable()
                                                            .idle(Duration.ofSeconds(TCP_KEEPALIVE_IDLE))
                                                            .interval(Duration.ofSeconds(TCP_KEEPALIVE_IDLE / 3))
                                                            .count(3)
                                                            .build();

        TcpUserTimeoutOptions tcpUserTimeoutOptions = TcpUserTimeoutOptions.builder()
                                                                           .enable()
                                                                           .tcpUserTimeout(
                                                                                   Duration.ofSeconds(TCP_USER_TIMEOUT)
                                                                           )
                                                                           .build();
        SocketOptions socketOptions = SocketOptions.builder()
                                                   .keepAlive(keepAliveOptions)
                                                   .tcpUserTimeout(tcpUserTimeoutOptions)
                                                   .build();

        return builder -> {
            builder.clientOptions(
                    ClientOptions.builder()
                                 .socketOptions(socketOptions)
                                 .build()
            );
        };
    }
}
