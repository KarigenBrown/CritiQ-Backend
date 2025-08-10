package me.critiq.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.critiq.backend.domain.entity.SeckillVoucher;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀优惠券表,与优惠券是一对一关系(SeckillVoucher)表数据库访问层
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:08:56
 */
@Mapper
public interface SeckillVoucherMapper extends BaseMapper<SeckillVoucher> {

}

