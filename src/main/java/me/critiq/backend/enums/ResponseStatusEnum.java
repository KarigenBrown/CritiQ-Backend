package me.critiq.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseStatusEnum {
    // 成功
    SUCCESS(200, "操作成功"),
    // 登录
    NEED_LOGIN(401, "需要登录后操作"),
    NO_OPERATOR_AUTH(403, "无权限操作"),
    SYSTEM_ERROR(500, "出现错误"),
    USERNAME_EXIST(501, "用户名已存在"),
    PHONE_NUMBER_EXIST(502, "手机号已存在"),
    EMAIL_EXIST(503, "邮箱已存在"),
    REQUIRE_USERNAME(504, "必需填写用户名"),
    LOGIN_ERROR(505, "用户名或密码错误"),
    CONTENT_NOT_NULL(506, "评论内容不能为空"),
    FILE_TYPE_ERROR(507, "文件类型错误,请上传png,jpg,jpeg文件"),
    USERNAME_NOT_NULL(508, "用户名不能为空"),
    NICKNAME_NOT_NULL(509, "昵称不能为空"),
    PASSWORD_NOT_NULL(510, "密码不能为空"),
    EMAIL_NOT_NULL(511, "邮箱不能为空"),
    NICKNAME_EXIST(512, "昵称已存在"),
    INVALID_EMAIL(513, "邮箱不合法"),
    CODE_ERROR(514, "验证码错误"),
    SHOP_NOT_FOUND(515, "商店不存在"),
    ID_NOT_NULL(516, "id不能为空"),
    NOT_FOUND(517, "查询结果不存在"),
    SECKILL_NOT_STARTED(518, "秒杀尚未开始"),
    SECKILL_ENDED(519, "秒杀已经结束"),
    STOCK_NOT_ENOUGH(520, "库存不足"),
    REPEAT_PURCHASE(521, "用户已经购买过了"),
    ;

    private final Integer code;
    private final String message;
}
