package com.qm.admin.config.mybatis;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.qm.admin.system.security.TenantIsolation;
import com.qm.admin.system.util.SecurityUtils;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Locale;

/**
 * @author zjf
 */
@Configuration
@EnableConfigurationProperties(QmTenantProperties.class)
public class MybatisPlusConfig {


    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(QmTenantProperties qmTenantProperties) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                Long tenantId = SecurityUtils.getCurrentTenantId();
                if (tenantId == null) {
                    throw new IllegalStateException("Tenant context is required for tenant-scoped SQL");
                }
                return new LongValue(tenantId);
            }

            @Override
            public boolean ignoreTable(String tableName) {
                // 平台表不属于任何租户；认证前的跨租户定位只允许走受控的认证链路。
                List<String> ignoreTables = qmTenantProperties.getIgnoreTables();
                return TenantIsolation.isIgnored() || ignoreTables.contains(tableName) || ignoreTables.contains(tableName.toLowerCase(Locale.ROOT));
            }
        }));
        // Keep write safety interceptors in the template so business projects inherit sane defaults.
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
