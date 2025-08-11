package me.critiq.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.critiq.backend.domain.entity.ThirdUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * (ThirdUser)表数据库访问层
 *
 * @author Karigen Brown
 * @since 2025-08-11 22:36:12
 */
@Mapper
public interface ThirdUserMapper extends BaseMapper<ThirdUser> {

}

