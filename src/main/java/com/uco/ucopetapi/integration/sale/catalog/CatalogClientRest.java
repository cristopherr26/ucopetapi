package com.uco.ucopetapi.integration.sale.catalog;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class CatalogClientRest implements CatalogClient {

    private final RestClient restClient = RestClient.builder()
            .requestInterceptor((request, body, execution) -> {
                var attributes =
                        org.springframework.web.context.request.RequestContextHolder
                                .getRequestAttributes();

                if (attributes instanceof org.springframework.web.context.request.ServletRequestAttributes servletAttributes) {
                    String authorization = servletAttributes.getRequest()
                            .getHeader("Authorization");

                    if (authorization != null && !authorization.isBlank()) {
                        request.getHeaders().set("Authorization", authorization);
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
    public CatalogItemResponse getProduct(UUID productId, UUID headquarterId) {
        return restClient.get()
                .uri(productsServiceUrl + "/{id}?headquarterId={headquarterId}",
                        productId, headquarterId)
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
