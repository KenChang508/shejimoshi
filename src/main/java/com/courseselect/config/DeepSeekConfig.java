package com.courseselect.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DeepSeek API 配置属性，映射 application.yml 中的 deepseek.api.*
 */
@Data
@ConfigurationProperties(prefix = "deepseek.api")
public class DeepSeekConfig {

    /** API Key */
    private String key;

    /** API 基础地址 */
    private String baseUrl = "https://api.deepseek.com";

    /** 模型名称 */
    private String model = "deepseek-chat";

    /** 最大 token 数 */
    private int maxTokens = 2000;

    /** 温度参数 */
    private double temperature = 0.7;
}
