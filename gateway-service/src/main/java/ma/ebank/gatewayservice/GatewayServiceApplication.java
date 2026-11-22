package ma.ebank.gatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }

    /**
     * Dynamic route locator based on Eureka Discovery Service.
     * Allows routing via /{service-id}/... (both lowercase and uppercase).
     */
    @Bean
    DiscoveryClientRouteDefinitionLocator dynamicRoutes(
            ReactiveDiscoveryClient rdc,
            DiscoveryLocatorProperties dlp) {
        dlp.setLowerCaseServiceId(true);
        return new DiscoveryClientRouteDefinitionLocator(rdc, dlp);
    }
}
