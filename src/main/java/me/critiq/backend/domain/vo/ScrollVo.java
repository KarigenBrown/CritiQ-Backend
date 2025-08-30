package me.critiq.backend.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ScrollVo<T> {
    private List<T> list;
    private Long minTime;
    private Integer offset;
}
