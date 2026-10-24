package com.mdkj.fallback;

import com.mdkj.entity.User;
import com.mdkj.feign.UserFeign;
import com.mdkj.result.Result;
import org.springframework.stereotype.Component;

/**
 * @author 油桃老师
 **/
@Component
public class UserFeignFallBack implements UserFeign {

    @Override
    public Result<User> select(Long id) {
        System.err.println("用户微服务远程调用失败 请联系管理员");
        return null;
    }
}
