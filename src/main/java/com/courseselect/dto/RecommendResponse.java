package com.courseselect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * AI 课程推荐响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendResponse {

    /** 学生学号 */
    private String studentNo;

    /** 学生姓名 */
    private String studentName;

    /** 推荐学期 */
    private String semester;

    /** 推荐结果列表 */
    private List<RecommendItem> recommendations;

    /** AI 给出的总体分析 */
    private String analysis;

    // ---------------------------------------------------------------
    // 单条推荐项
    // ---------------------------------------------------------------
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecommendItem {

        /** 课程代码 */
        private String courseCode;

        /** 课程名称 */
        private String courseName;

        /** 匹配度评分 0-100 */
        private int score;

        /** 推荐理由 */
        private String reason;
    }
}
