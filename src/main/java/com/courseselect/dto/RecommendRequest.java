package com.courseselect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 课程推荐请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendRequest {

    /** 学生学号 */
    @NotBlank(message = "学号不能为空")
    private String studentNo;

    /** 目标学期，如 "2025-2026-2" */
    @NotBlank(message = "学期不能为空")
    private String semester;

    /** 期望推荐数量，默认 5 */
    @Builder.Default
    private int limit = 5;
}
