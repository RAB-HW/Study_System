package com.hzb.erp.api.base.configuration;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.hzb.erp.api.base.dataScope.DataScopeInterceptor;
import com.hzb.erp.configuration.SystemConfig;
import com.hzb.erp.utils.PropertyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Ryan
 */
@Configuration
public class MybatisPlusConfig {

    @Autowired
    private SystemConfig systemConfig;

    /**
     * 新的分页插件,一缓和二缓遵循  mybatis的规则,需要设置 MybatisConfiguration#useDeprecatedExecutor = false
     * 避免缓存出现问题(该属性会在旧插件移除后一同移除)
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 数据权限 一定放到第一位执行，否则分页不受数据权限控制
        interceptor.addInnerInterceptor(dataScopeInterceptor());

        // 分页
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }

    public DataScopeInterceptor dataScopeInterceptor() {
        return new DataScopeInterceptor();
    }

}