package me.critiq.backend.domain.bo;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class RedisBo {
    private Instant expiration;
    private Object data;
}
