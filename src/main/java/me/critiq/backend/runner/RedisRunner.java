package me.critiq.backend.runner;

import lombok.RequiredArgsConstructor;
import me.critiq.backend.service.ShopService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisRunner implements CommandLineRunner {
    private final ShopService shopService;

    @Override
    public void run(String... args) throws Exception {
        shopService.saveShop2Redis(1L, 10L);
    }
}
