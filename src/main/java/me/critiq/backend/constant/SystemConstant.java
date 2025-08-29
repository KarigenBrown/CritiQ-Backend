package me.critiq.backend.constant;

public class SystemConstant {
    public static final String CODE = "code";
    public static final String USER = "user";
    public static final String LEVEL = "level";
    public static final String LEVEL_ = "level_";
    public static final String SELF = "self";
    public static final String ID = "id";
    public static final String USER_ = "user_";
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:user:";
    public static final Long LOGIN_USER_TTL = 30L;
    public static final String CACHE_SHOP_KEY = "cache:shop:";
    public static final Long CACHE_SHOP_TTL = 30L;
    public static final String CACHE_SHOP_TYPE_KEY = "cache:shop:type";
    public static final String CACHE_NULL_VALUE = "";
    public static final Long CACHE_NULL_TTL = 2L;
    public static final String LOCK_SHOP_KEY = "lock:shop:";
    public static final Long LOCK_SHOP_TTL = 10L;
    public static final String LOCK_ORDER_KEY = "lock:order:";
    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
    public static final String SECKILL_ORDER_KEY = "seckill:order:";
    public static final String STREAM_NAME = "stream.orders";
    public static final String QUEUE_NAME = "queue.orders";
    public static final String BUCKET_NAME = "critiq";
    public static final Long MAX_PAGE_SIZE = 10L;
    public static final String BLOG_LIKED_KEY = "blog:liked:";

    private SystemConstant() {
    }
}
