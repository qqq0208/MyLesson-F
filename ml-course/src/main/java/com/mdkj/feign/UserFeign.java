package com.mdkj.feign;

import com.mdkj.entity.User;
import com.mdkj.fallback.UserFeignFallBack;
import com.mdkj.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 向ml-user服务 发送请求的feign接口
 */
@FeignClient(value = "ml-user",fallback = UserFeignFallBack.class)
public interface UserFeign {

    //根据用户ID查询用户信息
    @GetMapping("/api/v1/user/select/{id}")
    Result<User> select(@PathVariable("id") Long id);
}
