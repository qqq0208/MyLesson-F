package com.mdkj.feign;

import com.mdkj.entity.Course;
import com.mdkj.fallback.CourseFeignFallBack;
import com.mdkj.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "ml-course",fallback = CourseFeignFallBack.class)
public interface CourseFeign {

    @GetMapping("/api/v1/course/select/{id}")
    Result<Course> select(@PathVariable("id") Long id);
}
