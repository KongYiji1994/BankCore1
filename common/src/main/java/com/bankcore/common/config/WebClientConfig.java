package com.bankcore.common.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(WebClientProperties.class)
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder(WebClientProperties properties,
                                              @Value("${spring.application.name:bankcore-service}") String applicationName) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) properties.getConnectTimeout().toMillis())
                .responseTimeout(properties.getResponseTimeout())
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(properties.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(properties.getWriteTimeout().toMillis(), TimeUnit.MILLISECONDS)));

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize((int) properties.getMaxInMemorySize().toBytes()))
                .build();

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, applicationName + "-webclient")
                .filter(errorHandlingFilter());
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }

    private ExchangeFilterFunction errorHandlingFilter() {
        return (request, next) -> next.exchange(request)
                .flatMap(response -> {
                    if (response.statusCode().isError()) {
                        return response.bodyToMono(String.class)
                                .defaultIfEmpty(response.statusCode().getReasonPhrase())
                                .flatMap(body -> Mono.error(new IllegalStateException(
                                        String.format("WebClient call to %s failed with status %s and body: %s",
                                                request.url(),
                                                response.statusCode(),
                                                truncate(body)))));
                    }
                    return Mono.just(response);
                });
    }

    private String truncate(String body) {
        if (body == null) {
            return "";
        }
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        int limit = Math.min(bytes.length, 2048);
        return new String(bytes, 0, limit, StandardCharsets.UTF_8);
    }
}
