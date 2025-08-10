package me.critiq.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.critiq.backend.domain.entity.BlogComments;
import org.apache.ibatis.annotations.Mapper;

/**
 * (BlogComments)表数据库访问层
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:08:54
 */
@Mapper
public interface BlogCommentsMapper extends BaseMapper<BlogComments> {

}

