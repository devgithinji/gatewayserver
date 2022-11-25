package com.densoft.gatewayserver.filters;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

//the custom routing configurations can be also done via the .properties file or .yaml file but this
//is left for dynamic changes since the new configurations can be refreshed via the call of the actuator refresh path
//this project focuses on the java config

@Order(1)
@Component
public class TraceFilter implements GlobalFilter {

    private static final Logger logger = LoggerFactory.getLogger(TraceFilter.class);

    @Autowired
    FilterUtility filterUtility;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders httpHeaders = exchange.getRequest().getHeaders();
        if (isCorrelationIdPresent(httpHeaders)) {
            logger.debug("EazyBank-correlation-id found in tracing filter: {}. ", filterUtility.getCorrelationId(httpHeaders));
        } else {
            String correlationId = generateCorrelationId();
            exchange = filterUtility.setCorrelationId(exchange, correlationId);
            logger.debug("EazyBank-correlation-id generated in tracing filter: {}.", correlationId);
        }
        return chain.filter(exchange);
    }

    private boolean isCorrelationIdPresent(HttpHeaders httpHeaders) {
        if (filterUtility.getCorrelationId(httpHeaders) != null) {
            return true;
        }
        return false;
    }

    private  String generateCorrelationId(){
        return UUID.randomUUID().toString();
    }
}
