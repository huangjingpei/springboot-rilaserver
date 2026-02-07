package net.enjoy.springboot.registrationlogin.config; // <-- 务必！！！替换成您项目的主包名

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 这个方法是解决MIME类型问题的核心。
     * @EnableWebMvc 可能会覆盖Spring Boot的默认静态资源配置，
     * 因此我们必须手动重新定义静态资源处理器。
     * 当Spring通过这个处理器提供文件时，它会为.js等文件自动设置正确的Content-Type。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}