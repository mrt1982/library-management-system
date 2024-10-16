package com.identitye2e.library.configuration;

import com.identitye2e.library.rest.v1.LibraryResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        register(LibraryResource.class);
        registerClasses(ExceptionHandlersConfig.class.getClasses());
        property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
    }
}
