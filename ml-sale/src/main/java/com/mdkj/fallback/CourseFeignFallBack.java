package com.mdkj.fallback;

import com.mdkj.entity.Course;
import com.mdkj.feign.CourseFeign;
import com.mdkj.result.Result;
import org.springframework.stereotype.Component;

/**
 * @author 油桃老师
 **/
@Component
public class CourseFeignFallBack implements CourseFeign {
    @Override
    public Result<Course> select(Long id) {
        System.out.println("远程查询课程失败====>");
        return null;
    }
}
