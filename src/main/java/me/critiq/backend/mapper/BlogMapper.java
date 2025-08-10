package me.critiq.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.critiq.backend.domain.entity.Blog;
import org.apache.ibatis.annotations.Mapper;

/**
 * (Blog)表数据库访问层
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:08:10
 */
@Mapper
public interface BlogMapper extends BaseMapper<Blog> {

}

