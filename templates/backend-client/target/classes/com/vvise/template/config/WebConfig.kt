package com.vvise.template.config

import org.springframework.context.annotation.Configuration
import org.springframework.http.CacheControl
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.concurrent.TimeUnit

/**
 * Web configuration for serving static resources.
 * Configures cache headers for assets and static files.
 */
@Configuration
class WebConfig : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // Serve assets with long cache (they have hashed filenames)
        registry.addResourceHandler("/assets/**")
            .addResourceLocations("classpath:/static/assets/")
            .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic())

        // Serve other static files with shorter cache
        registry.addResourceHandler("/*.js", "/*.css", "/*.json", "/*.ico", "/*.svg", "/*.png")
            .addResourceLocations("classpath:/static/")
            .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
    }
}
