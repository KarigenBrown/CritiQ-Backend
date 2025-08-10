package me.critiq.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.critiq.backend.mapper.BlogMapper;
import me.critiq.backend.domain.entity.Blog;
import me.critiq.backend.service.BlogService;
import org.springframework.stereotype.Service;

/**
 * (Blog)表服务实现类
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:15
 */
@Service("blogService")
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

}

