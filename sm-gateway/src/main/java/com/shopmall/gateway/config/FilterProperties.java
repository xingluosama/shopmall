package com.shopmall.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "sm.filter")
public class FilterProperties {

    private List<String> allowPaths;
}
