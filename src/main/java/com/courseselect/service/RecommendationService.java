package com.courseselect.service;

import com.courseselect.client.DeepSeekClient;
import com.courseselect.client.DeepSeekClient.DeepSeekException;
import com.courseselect.dto.RecommendResponse;
import com.courseselect.dto.RecommendResponse.RecommendItem;
import com.courseselect.entity.Course;
import com.courseselect.entity.Student;
import com.courseselect.entity.Teacher;
import com.courseselect.repository.SelectionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI 驱动的课程推荐引擎。
 *
 * 推荐流程：
 * 1. 收集学生画像（专业、年级、GPA、兴趣、已修课程）
 * 2. 加载目标学期可选课程
 * 3. 尝试调用 DeepSeek AI 获取推荐（带降级）
 * 4. 解析 AI 返回的 JSON 推荐结果
 * 5. 执行确定性校验（先修课、课容量），修正推荐
 * 6. AI 不可用时自动切换本地规则引擎
 */
@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final DeepSeekClient deepSeekClient;
    private final StudentService studentService;
    private final CourseService courseService;
    private final SelectionRepository selectionRepository;
    private final ObjectMapper objectMapper;

    public RecommendationService(DeepSeekClient deepSeekClient,
                                 StudentService studentService,
                                 CourseService courseService,
                                 SelectionRepository selectionRepository,
                                 ObjectMapper objectMapper) {
        this.deepSeekClient = deepSeekClient;
        this.studentService = studentService;
        this.courseService = courseService;
        this.selectionRepository = selectionRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * 为学生推荐课程（AI 优先，失败时本地规则兜底）
     */
    public RecommendResponse recommend(String studentNo, String semester, int limit) {
        // ─── 1. 加载学生信息 ───
        Student student = studentService.findByStudentNo(studentNo);

        // ─── 2. 加载可选课程 ───
        List<Course> availableCourses = courseService.findAvailable(semester);
        if (availableCourses.isEmpty()) {
            return RecommendResponse.builder()
                    .studentNo(studentNo)
                    .studentName(student.getName())
                    .semester(semester)
                    .recommendations(Collections.emptyList())
                    .analysis("该学期暂无可选课程。")
                    .build();
        }

        // ─── 3. 获取已完成的课程代码 ───
        List<String> completedCodes = selectionRepository.findCompletedCourseCodes(student.getId());

        // ─── 4. 尝试 AI 推荐，失败则降级 ───
        try {
            return aiRecommend(student, semester, availableCourses, completedCodes, limit);
        } catch (DeepSeekException e) {
            log.warn("AI 推荐失败，降级为本地规则: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("AI 推荐异常，降级为本地规则", e);
        }

        // ─── 5. 本地规则兜底 ───
        List<RecommendItem> localItems = localRuleRecommend(
                student, availableCourses, completedCodes, limit);
        return RecommendResponse.builder()
                .studentNo(studentNo)
                .studentName(student.getName())
                .semester(semester)
                .recommendations(localItems)
                .analysis("（本地规则推荐）基于专业匹配、兴趣方向、先修课要求与学分规划智能排序。"
                        + "配置 DeepSeek API Key 后可启用 AI 个性化推荐。")
                .build();
    }

    // ================================================================
    // AI 推荐流程
    // ================================================================

    private RecommendResponse aiRecommend(Student student, String semester,
                                           List<Course> availableCourses,
                                           List<String> completedCodes, int limit) {
        String systemPrompt = buildSystemPrompt();
        String userMessage = buildUserMessage(student, availableCourses, completedCodes, limit);

        String aiResponse = deepSeekClient.chat(systemPrompt, userMessage);
        AIRecommendResult aiResult = parseAIResponse(aiResponse);

        List<RecommendItem> validated = validateAndRank(
                aiResult, availableCourses, completedCodes, limit);

        return RecommendResponse.builder()
                .studentNo(student.getStudentNo())
                .studentName(student.getName())
                .semester(semester)
                .recommendations(validated)
                .analysis(aiResult.analysis())
                .build();
    }

    // ================================================================
    // Prompt 构建
    // ================================================================

    private String buildSystemPrompt() {
        return """
                你是一个大学智能选课助手。你需要根据学生的专业背景、年级、兴趣方向和已修课程，
                从可选课程列表中推荐最合适的课程。请严格按照 JSON 格式返回结果。

                推荐时请考虑以下因素：
                1. 专业匹配度：课程是否与学生专业相关
                2. 年级适配：课程难度是否适合当前年级
                3. 兴趣匹配：课程内容是否与学生兴趣方向吻合
                4. 先修课要求：学生是否已修完课程所需的先修课
                5. 学分规划：推荐的课程学分总和应合理（一般每学期 15-25 学分）
                6. 课程组合：推荐的课程应形成互补的知识体系，避免时间冲突

                请返回如下 JSON 格式：
                {
                  "analysis": "整体推荐理由与分析，200字以内",
                  "recommendations": [
                    {
                      "courseCode": "CS301",
                      "courseName": "操作系统",
                      "score": 95,
                      "reason": "推荐理由，50字以内"
                    }
                  ]
                }

                score 取值范围 0-100，分数越高表示越推荐。
                """;
    }

    private String buildUserMessage(Student student, List<Course> courses,
                                     List<String> completedCodes, int limit) {
        StringBuilder sb = new StringBuilder();
        sb.append("## 学生信息\n");
        sb.append("- 学号: ").append(student.getStudentNo()).append("\n");
        sb.append("- 姓名: ").append(student.getName()).append("\n");
        sb.append("- 专业: ").append(student.getMajor()).append("\n");
        sb.append("- 年级: ").append(student.getGrade()).append("级\n");
        sb.append("- GPA: ").append(student.getGpa() != null ? student.getGpa() : "暂无").append("\n");
        sb.append("- 兴趣方向: ").append(
                student.getInterests() != null ? student.getInterests() : "未填写").append("\n");
        sb.append("- 已修学分: ").append(student.getCompletedCredits()).append("\n");
        sb.append("- 已完成课程: ").append(
                completedCodes.isEmpty() ? "无" : String.join(", ", completedCodes)).append("\n\n");

        sb.append("## 可选课程列表（共 ").append(courses.size()).append(" 门）\n\n");
        for (Course c : courses) {
            sb.append("---\n");
            sb.append("课程代码: ").append(c.getCourseCode()).append("\n");
            sb.append("课程名称: ").append(c.getName()).append("\n");
            sb.append("学分: ").append(c.getCredits()).append("\n");
            sb.append("类别: ").append(c.getCategory()).append("\n");
            sb.append("上课时间: ").append(c.getSchedule()).append("\n");
            sb.append("上课地点: ").append(c.getLocation()).append("\n");
            sb.append("先修课要求: ").append(
                    c.getPrerequisites() != null ? c.getPrerequisites() : "无").append("\n");
            sb.append("已选/容量: ").append(c.getEnrolled()).append("/").append(c.getCapacity()).append("\n");
            sb.append("课程简介: ").append(c.getDescription() != null ? c.getDescription() : "暂无").append("\n");
            sb.append("授课教师: ").append(
                    c.getTeacher() != null
                            ? c.getTeacher().getName() + " ("
                                    + (c.getTeacher().getTitle() != null ? c.getTeacher().getTitle() : "")
                                    + ")"
                            : "待定").append("\n");
        }

        sb.append("\n请从以上课程中推荐最合适的 ").append(limit).append(" 门课程。");
        return sb.toString();
    }

    // ================================================================
    // AI 响应解析
    // ================================================================

    private AIRecommendResult parseAIResponse(String jsonContent) {
        try {
            Map<String, Object> map = objectMapper.readValue(jsonContent,
                    new TypeReference<Map<String, Object>>() {});

            String analysis = (String) map.getOrDefault("analysis", "");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> recs = (List<Map<String, Object>>) map.get("recommendations");
            if (recs == null) recs = Collections.emptyList();

            List<RecommendItem> items = new ArrayList<>();
            for (Map<String, Object> rec : recs) {
                items.add(RecommendItem.builder()
                        .courseCode((String) rec.get("courseCode"))
                        .courseName((String) rec.get("courseName"))
                        .score(toInt(rec.get("score")))
                        .reason((String) rec.get("reason"))
                        .build());
            }

            return new AIRecommendResult(analysis, items);

        } catch (Exception e) {
            log.warn("AI 响应解析失败: {}", e.getMessage());
            return new AIRecommendResult("AI 返回结果格式异常，已启用本地规则推荐。", Collections.emptyList());
        }
    }

    // ================================================================
    // 确定性校验
    // ================================================================

    private List<RecommendItem> validateAndRank(AIRecommendResult aiResult,
                                                 List<Course> courses,
                                                 List<String> completedCodes,
                                                 int limit) {
        Map<String, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getCourseCode, c -> c, (a, b) -> a));

        List<RecommendItem> result = new ArrayList<>();

        for (RecommendItem item : aiResult.recommendations()) {
            Course course = courseMap.get(item.getCourseCode());
            if (course == null) continue;

            int adjustedScore = item.getScore();

            // 先修课校验
            if (course.getPrerequisites() != null && !course.getPrerequisites().isBlank()) {
                boolean allMet = Arrays.stream(course.getPrerequisites().split(","))
                        .map(String::trim)
                        .allMatch(completedCodes::contains);
                if (!allMet) {
                    adjustedScore = Math.max(0, adjustedScore - 30);
                    item.setReason(item.getReason() + "（注意：部分先修课未完成）");
                }
            }

            // 课容量校验
            if (course.getEnrolled() >= course.getCapacity()) {
                adjustedScore = Math.max(0, adjustedScore - 40);
                item.setReason(item.getReason() + "（注意：课程已满）");
            }

            item.setScore(Math.max(0, Math.min(100, adjustedScore)));
            result.add(item);
        }

        // AI 推荐数量不足时，本地规则补充
        if (result.size() < limit) {
            Set<String> aiCodes = result.stream()
                    .map(RecommendItem::getCourseCode)
                    .collect(Collectors.toSet());

            courses.stream()
                    .filter(c -> !aiCodes.contains(c.getCourseCode()))
                    .sorted(Comparator
                            .comparingInt(Course::getCredits).reversed()
                            .thenComparing(c -> c.getEnrolled()))
                    .limit(limit - result.size())
                    .forEach(c -> result.add(RecommendItem.builder()
                            .courseCode(c.getCourseCode())
                            .courseName(c.getName())
                            .score(60)
                            .reason("（本地补充）尚有 " + (c.getCapacity() - c.getEnrolled()) + " 个名额")
                            .build()));
        }

        result.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        return result.size() > limit ? result.subList(0, limit) : result;
    }

    // ================================================================
    // 本地规则推荐（AI 不可用时的完全 fallback）
    // ================================================================

    private List<RecommendItem> localRuleRecommend(Student student, List<Course> courses,
                                                    List<String> completedCodes, int limit) {
        String major = student.getMajor();
        Set<String> interestSet = parseInterests(student.getInterests());

        List<ScoredCourse> scored = new ArrayList<>();

        for (Course course : courses) {
            int score = 50;

            score += majorMatchScore(major, course);          // 0-20
            score += interestMatchScore(interestSet, course);  // 0-15
            score += prerequisiteScore(course, completedCodes); // -10-15

            int remaining = course.getCapacity() - course.getEnrolled();
            score += Math.min(10, remaining * 2);              // 0-10

            if ("必修".equals(course.getCategory())) {
                score += 10;                                   // 必修优先
            } else {
                score += 5;
            }

            StringBuilder reason = new StringBuilder();
            reason.append("匹配专业 ").append(shortMajor(major));
            if (!interestSet.isEmpty() && interestMatchScore(interestSet, course) >= 10) {
                reason.append("，契合兴趣");
            }
            if (course.getPrerequisites() == null || course.getPrerequisites().isBlank()) {
                reason.append("，无先修要求");
            }
            reason.append("，剩余 ").append(remaining).append(" 名额");

            scored.add(new ScoredCourse(course, score, reason.toString()));
        }

        return scored.stream()
                .sorted((a, b) -> Integer.compare(b.score, a.score))
                .limit(limit)
                .map(sc -> RecommendItem.builder()
                        .courseCode(sc.course.getCourseCode())
                        .courseName(sc.course.getName())
                        .score(sc.score)
                        .reason(sc.reason)
                        .build())
                .toList();
    }

    private Set<String> parseInterests(String interests) {
        if (interests == null || interests.isBlank()) return Collections.emptySet();
        return Arrays.stream(interests.split("[,，]"))
                .map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    /** 专业名缩写（用于展示） */
    private String shortMajor(String major) {
        if (major == null) return "";
        return major.length() > 8 ? major.substring(0, 8) + "..." : major;
    }

    private int majorMatchScore(String major, Course course) {
        if (major == null) return 10;
        Teacher teacher = course.getTeacher();
        if (teacher != null && teacher.getDepartment() != null
                && teacher.getDepartment().contains(major)) {
            return 20;
        }
        String name = course.getName() != null ? course.getName() : "";
        String desc = course.getDescription() != null ? course.getDescription() : "";
        if (name.contains(major) || desc.contains(major)) return 15;
        return 5;
    }

    private int interestMatchScore(Set<String> interests, Course course) {
        if (interests.isEmpty()) return 8;
        String haystack = (course.getName() + " " + course.getDescription()).toLowerCase();
        long matched = interests.stream()
                .filter(i -> haystack.contains(i.toLowerCase()))
                .count();
        return matched > 0 ? 15 : 3;
    }

    private int prerequisiteScore(Course course, List<String> completedCodes) {
        if (course.getPrerequisites() == null || course.getPrerequisites().isBlank()) return 10;
        boolean allMet = Arrays.stream(course.getPrerequisites().split(","))
                .map(String::trim).allMatch(completedCodes::contains);
        return allMet ? 15 : -10;
    }

    private int toInt(Object value) {
        if (value instanceof Integer i) return i;
        if (value instanceof Number n) return n.intValue();
        if (value instanceof String s) {
            try { return Integer.parseInt(s); } catch (NumberFormatException e) { return 0; }
        }
        return 0;
    }

    // ---------------------------------------------------------------
    // 内部数据类
    // ---------------------------------------------------------------
    private record AIRecommendResult(String analysis, List<RecommendItem> recommendations) {}
    private record ScoredCourse(Course course, int score, String reason) {}
}