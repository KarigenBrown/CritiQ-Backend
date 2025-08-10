package me.critiq.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.critiq.backend.domain.entity.Follow;
import org.apache.ibatis.annotations.Mapper;

/**
 * (Follow)表数据库访问层
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:08:56
 */
@Mapper
public interface FollowMapper extends BaseMapper<Follow> {

}

