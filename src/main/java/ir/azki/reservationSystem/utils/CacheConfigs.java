package ir.azki.reservationSystem.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import ir.azki.reservationSystem.entities.AvailableSlot;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Optional;

@Configuration
@EnableCaching
public class CacheConfigs {

    @Bean
    public Cache<String, Optional<AvailableSlot>> availableSlotCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofSeconds(10))
                .build();
    }
}
