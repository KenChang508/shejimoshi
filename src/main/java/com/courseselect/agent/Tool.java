package com.courseselect.agent;

import java.util.Map;

/**
 * 智能体可调用工具接口。
 * 每个工具对应一个可被大模型通过 Function Calling 调用的后端操作。
 */
public interface Tool {

    /** 工具名称（须与 function definition 中的 name 一致） */
    String getName();

    /** 工具描述（告知大模型何时调用此工具） */
    String getDescription();

    /** 参数 JSON Schema（含 type / properties / required） */
    Map<String, Object> getParametersSchema();

    /**
     * 执行工具。
     * @param parameters 大模型传回的参数 Map
     * @return 执行结果（JSON 字符串或可读文本）
     */
    String execute(Map<String, Object> parameters);
}
