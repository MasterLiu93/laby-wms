package com.laby.module.wms.framework.web.config;

import com.laby.framework.swagger.config.LabySwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * wms 模块的 web 组件的 Configuration
 *
 * @author 芋道源码
 */
@Configuration(proxyBeanMethods = false)
public class WmsWebConfiguration {

    /**
     * wms 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi wmsGroupedOpenApi() {
        return LabySwaggerAutoConfiguration.buildGroupedOpenApi("wms");
    }

}
