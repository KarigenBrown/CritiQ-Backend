package me.critiq.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.critiq.backend.domain.entity.Voucher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (Voucher)表数据库访问层
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:08:56
 */
@Mapper
public interface VoucherMapper extends BaseMapper<Voucher> {
    List<Voucher> queryVoucherOfShop(@Param("shopId") Long shopId);
}

