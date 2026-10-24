package com.mdkj.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author 油桃老师
 **/
@Component
public class TestJob {

    @XxlJob("testyoutao")
    public void youtaotest(){
        System.out.println("定时调度任务执行了====>"+ LocalDateTime.now());
    }
}
