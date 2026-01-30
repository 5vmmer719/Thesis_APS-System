package com.aps.constant;

/**
 * 错误码常量
 */
public interface ErrorCode {

    // ========== 客户端错误 4xxxx ==========

    // 通用错误 400xx
    int BAD_REQUEST = 40000;           // 请求参数错误
    int DUPLICATE_DATA = 40001;        // 数据重复
    int DATA_NOT_FOUND = 40002;        // 数据不存在
    int OPERATION_FORBIDDEN = 40003;   // 操作被禁止

    // 认证错误 401xx
    int UNAUTHORIZED = 40100;          // 未登录
    int TOKEN_INVALID = 40101;         // Token无效
    int TOKEN_EXPIRED = 40102;         // Token过期
    int PASSWORD_ERROR = 40103;        // 密码错误

    // 权限错误 403xx
    int FORBIDDEN = 40300;             // 无权限
    int ROLE_FORBIDDEN = 40301;        // 角色权限不足

    // 资源错误 404xx
    int NOT_FOUND = 40401;             // 资源不存在
    int USER_NOT_FOUND = 40402;        // 用户不存在

    // ========== 服务端错误 5xxxx ==========

    int INTERNAL_ERROR = 50000;        // 系统内部错误
    int DATABASE_ERROR = 50001;        // 数据库错误
    int EXTERNAL_SERVICE_ERROR = 50002; // 外部服务调用失败
}

