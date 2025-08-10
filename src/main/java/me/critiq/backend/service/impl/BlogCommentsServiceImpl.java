package me.critiq.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.critiq.backend.mapper.BlogCommentsMapper;
import me.critiq.backend.domain.entity.BlogComments;
import me.critiq.backend.service.BlogCommentsService;
import org.springframework.stereotype.Service;

/**
 * (BlogComments)表服务实现类
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:24
 */
@Service("blogCommentsService")
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements BlogCommentsService {

}

