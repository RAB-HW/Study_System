package com.hzb.erp.api.base.configuration;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.hzb.erp.api.pc.clazz.entity.ClassStudent;
import com.hzb.erp.api.pc.clazz.entity.Clazz;
import com.hzb.erp.api.pc.clazz.mapper.ClassStudentMapper;
import com.hzb.erp.api.pc.clazz.mapper.ClazzMapper;
import com.hzb.erp.api.pc.lesson.service.LessonStudentService;
import com.hzb.erp.api.pc.student.entity.Student;
import com.hzb.erp.api.pc.student.mapper.StudentMapper;
import com.hzb.erp.api.pc.sys.mapper.CommonMapper;
import com.hzb.erp.configuration.SystemConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 系统定时任务配置 生产下可以注释掉该类
 */
@Configuration
@EnableScheduling
@Slf4j
public class CronConfigurer {

    @Resource
    private CommonMapper commonMapper;

    @Resource
    private SystemConfig systemConfig;

    /**
     * 每隔时间恢复一次数据 生产环境禁用！！！！！！！！！！！！！！
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    void revertData() {

        if (!systemConfig.getIsDemo()) {
            return;
        }

        System.out.println("恢复数据");
        commonMapper.revertData("contact_record");
        commonMapper.revertData("class_student");
        commonMapper.revertData("classroom");
        commonMapper.revertData("class");
        commonMapper.revertData("course");
        commonMapper.revertData("lesson");
        commonMapper.revertData("lesson_schedule");
        commonMapper.revertData("student");
        commonMapper.revertData("student_course");
        commonMapper.revertData("subject");
        commonMapper.revertStaff();
    }
}
