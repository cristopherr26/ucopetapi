
package com.uco.ucopetapi.integration.sale.catalog;

import com.uco.ucopetapi.security.AuthCookie;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Component
public class CatalogClientRest implements CatalogClient {

    private final RestClient restClient = RestClient.builder()
            .requestInterceptor((request, body, execution) -> {

                var attributes = RequestContextHolder
                        .getRequestAttributes();

                if (attributes instanceof ServletRequestAttributes servletAttributes) {

                    var incomingRequest = servletAttributes.getRequest();

                    String authorization = incomingRequest
                            .getHeader(HttpHeaders.AUTHORIZATION);

                    if (authorization != null && !authorization.isBlank()) {
                        request.getHeaders().set(
                                HttpHeaders.AUTHORIZATION,
                                authorization
                        );
                    }

                    Cookie[] cookies = incomingRequest.getCookies();

                    if (cookies != null) {
                        for (Cookie cookie : cookies) {

                            if (AuthCookie.NAME.equals(cookie.getName())
                                    && cookie.getValue() != null
                                    && !cookie.getValue().isBlank()) {

                                request.getHeaders().set(
                                        HttpHeaders.COOKIE,
                                        AuthCookie.NAME + "=" + cookie.getValue()
                                );

                                break;
                            }
                        }
                    }
                }

                return execution.execute(request, body);
            })
            .build();

    @Value("${products.service.url:http://localhost:8080/api/v1/products}")
    private String productsServiceUrl;

    @Value("${services.service.url:http://localhost:8080/api/v1/services}")
    private String servicesServiceUrl;

    @Override
    public CatalogItemResponse getProduct(
            UUID productId,
            UUID headquarterId
    ) {
        return restClient.get()
                .uri(
                        productsServiceUrl + "/{id}?headquarterId={headquarterId}",
                        productId,
                        headquarterId
                )
                .retrieve()
                .body(CatalogItemResponse.class);
    }

    @Override
    public CatalogItemResponse getService(UUID serviceId) {
        return restClient.get()
                .uri(servicesServiceUrl + "/{id}", serviceId)
                .retrieve()
                .body(CatalogItemResponse.class);
    }
}
