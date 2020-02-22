package top.leejay.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import top.leejay.springboot.chapter7.MyApplicationContextInitializer;

@SpringBootApplication
public class SpringbootApplication {
	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(SpringbootApplication.class);
		springApplication.addInitializers(new MyApplicationContextInitializer());
		springApplication.run(args);
	}
}
