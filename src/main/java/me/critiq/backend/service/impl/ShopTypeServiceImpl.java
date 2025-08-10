package me.critiq.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.critiq.backend.mapper.ShopTypeMapper;
import me.critiq.backend.domain.entity.ShopType;
import me.critiq.backend.service.ShopTypeService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * (ShopType)表服务实现类
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:27
 */
@Service("shopTypeService")
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements ShopTypeService {

}

