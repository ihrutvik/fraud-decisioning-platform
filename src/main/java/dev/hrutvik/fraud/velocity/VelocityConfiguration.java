package dev.hrutvik.fraud.velocity;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(VelocityProperties.class)
class VelocityConfiguration {}
