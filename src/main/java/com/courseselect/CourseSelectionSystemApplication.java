package com.courseselect;

import com.courseselect.config.DeepSeekConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DeepSeekConfig.class)
public class CourseSelectionSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseSelectionSystemApplication.class, args);
    }
}
