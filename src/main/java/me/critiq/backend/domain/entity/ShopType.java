package me.critiq.backend.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * (ShopType)表实体类
 *
 * @author Karigen Brown
 * @since 2025-08-10 15:31:22
 */
@Data
@Accessors(chain = true)
@TableName("shop_type")
@EqualsAndHashCode(callSuper = false)
public class ShopType {
    // 主键
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    // 类型名称
    private String name;
    // 图标
    private String icon;
    // 顺序
    private Integer sort;
    // 创建时间
    @JsonIgnore
    private LocalDateTime createTime;
    // 更新时间
    @JsonIgnore
    private LocalDateTime updateTime;
}

