package com.campusseekers.config;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "recommendation")
@Validated
@Getter
@Setter
public class RecommendationProperties {

    @NotNull(message = "safe-threshold must not be null")
    @DecimalMin(value = "0.0", message = "safe-threshold must be positive or zero")
    private BigDecimal safeThreshold = new BigDecimal("3.0");

    @NotNull(message = "target-threshold must not be null")
    @DecimalMin(value = "0.0", message = "target-threshold must be positive or zero")
    private BigDecimal targetThreshold = new BigDecimal("1.5");

    @NotNull(message = "dream-threshold must not be null")
    private BigDecimal dreamThreshold = new BigDecimal("0.0");

    @NotNull(message = "maximum-results must not be null")
    @Min(value = 1, message = "maximum-results must be at least 1")
    @Max(value = 500, message = "maximum-results cannot exceed 500")
    private Integer maximumResults = 50;

    @NotNull(message = "maximum-allowed-results must not be null")
    @Min(value = 1, message = "maximum-allowed-results must be at least 1")
    private Integer maximumAllowedResults = 100;

    @NotNull(message = "cache-minutes must not be null")
    @Min(value = 0, message = "cache-minutes must be positive or zero")
    private Integer cacheMinutes = 10;

    @NotBlank(message = "engine-version must not be blank")
    private String engineVersion = "1.0";

    @NotBlank(message = "algorithm-version must not be blank")
    private String algorithmVersion = "historical-cutoff-v1";
}
