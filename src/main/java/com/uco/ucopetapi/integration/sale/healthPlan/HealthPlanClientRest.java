
package com.uco.ucopetapi.integration.sale.healthPlan;

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
public class HealthPlanClientRest implements HealthPlanClient {

    private final RestClient restClient = RestClient.builder()
            .requestInterceptor((request, body, execution) -> {

                var attributes = RequestContextHolder
                        .getRequestAttributes();

                if (attributes instanceof ServletRequestAttributes servletAttributes) {

                    var incomingRequest =
                            servletAttributes.getRequest();

                    String authorization = incomingRequest
                            .getHeader(HttpHeaders.AUTHORIZATION);

                    if (authorization != null
                            && !authorization.isBlank()) {

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

    @Value("${healthplans.service.url:http://localhost:8080/api/v1/healthplans}")
    private String healthPlansServiceUrl;

    @Override
    public HealthPlanResponse getHealthPlan(UUID healthPlanId) {
        return restClient.get()
                .uri(
                        healthPlansServiceUrl + "/{id}",
                        healthPlanId
                )
                .retrieve()
                .body(HealthPlanResponse.class);
    }
}
