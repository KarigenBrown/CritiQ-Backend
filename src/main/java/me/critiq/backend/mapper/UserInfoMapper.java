package me.critiq.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.critiq.backend.domain.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * (UserInfo)表数据库访问层
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:08:56
 */
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {

}

