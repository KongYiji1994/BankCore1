package com.bankcore.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.time.Duration;

@ConfigurationProperties(prefix = "http.client")
public class WebClientProperties {
    /**
     * TCP connection timeout for the underlying HTTP client.
     */
    private Duration connectTimeout = Duration.ofSeconds(5);

    /**
     * Overall response timeout for a request.
     */
    private Duration responseTimeout = Duration.ofSeconds(10);

    /**
     * Read timeout after the connection is established.
     */
    private Duration readTimeout = Duration.ofSeconds(10);

    /**
     * Write timeout after the connection is established.
     */
    private Duration writeTimeout = Duration.ofSeconds(10);

    /**
     * Maximum in-memory buffer size for WebClient responses.
     */
    private DataSize maxInMemorySize = DataSize.ofMegabytes(10);

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getResponseTimeout() {
        return responseTimeout;
    }

    public void setResponseTimeout(Duration responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Duration getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public DataSize getMaxInMemorySize() {
        return maxInMemorySize;
    }

    public void setMaxInMemorySize(DataSize maxInMemorySize) {
        this.maxInMemorySize = maxInMemorySize;
    }
}
