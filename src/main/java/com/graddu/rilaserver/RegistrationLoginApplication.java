package net.enjoy.springboot.registrationlogin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableAsync
@EnableScheduling // 启用定时任务
@EnableRedisHttpSession // 启用Spring Session Redis支持
public class RegistrationLoginApplication {

	public static void main(String[] args) {
		SpringApplication.run(RegistrationLoginApplication.class, args);
	}

	/**
	 * 设置应用默认时区为中国标准时间
	 * 确保LocalDateTime.now()返回正确的北京时间
	 */
	@PostConstruct
	public void init() {
		// 设置应用默认时区为Asia/Shanghai（北京时间）
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		System.out.println("应用时区已设置为: " + TimeZone.getDefault().getID());
	}
}
