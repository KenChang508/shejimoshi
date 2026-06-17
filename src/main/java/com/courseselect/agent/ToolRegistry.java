package com.courseselect.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 工具注册中心 —— 汇总所有 Tool 实现并生成 DeepSeek Function Calling 所需的
 * tools 定义数组。
 */
@Component
public class ToolRegistry {

    private static final Logger log = LoggerFactory.getLogger(ToolRegistry.class);

    private final Map<String, Tool> tools = new LinkedHashMap<>();
    private final ObjectMapper objectMapper;

    /**
     * Spring 自动注入所有 Tool 实现并注册。
     */
    public ToolRegistry(List<Tool> toolList, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        for (Tool tool : toolList) {
            tools.put(tool.getName(), tool);
        }
        log.info("Agent 工具注册完成: {} 个 — {}", tools.size(), tools.keySet());
    }

    /**
     * 生成 Function Calling 所需 tools 数组。
     * 格式：[ { type:"function", function:{ name, description, parameters } }, ... ]
     */
    public List<Map<String, Object>> getFunctionDefinitions() {
        List<Map<String, Object>> defs = new ArrayList<>();
        for (Tool tool : tools.values()) {
            Map<String, Object> def = new LinkedHashMap<>();
            def.put("type", "function");

            Map<String, Object> func = new LinkedHashMap<>();
            func.put("name", tool.getName());
            func.put("description", tool.getDescription());
            func.put("parameters", tool.getParametersSchema());
            def.put("function", func);

            defs.add(def);
        }
        return defs;
    }

    /**
     * 根据名称和 JSON 参数串执行工具。
     */
    public String execute(String name, String arguments) {
        Tool tool = tools.get(name);
        if (tool == null) {
            return "{\"error\": \"未知工具: " + name + "\"}";
        }
        try {
            Map<String, Object> params = objectMapper.readValue(
                    arguments, new TypeReference<Map<String, Object>>() {});
            return tool.execute(params);
        } catch (Exception e) {
            log.error("工具 {} 执行失败: {}", name, e.getMessage());
            return "{\"error\": \"工具执行异常: " + e.getMessage() + "\"}";
        }
    }
}
