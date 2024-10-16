package com.identitye2e.library.application;

import com.identitye2e.library.configuration.ApplicationConfig;
import com.identitye2e.library.configuration.ExceptionHandlersConfig;
import com.identitye2e.library.configuration.JerseyConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

@Slf4j
@Import({
        ApplicationConfig.class,
        JerseyConfig.class,
        ExceptionHandlersConfig.class
})
@EnableAutoConfiguration
public class LibraryApplication {
    public LibraryApplication() {
        log.info("LibraryApplication APP INITIALISED version={}", getClass().getPackage().getImplementationVersion());
    }

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }
}
