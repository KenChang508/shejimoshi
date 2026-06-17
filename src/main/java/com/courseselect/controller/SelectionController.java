package com.courseselect.controller;

import com.courseselect.dto.ApiResponse;
import com.courseselect.dto.RecommendRequest;
import com.courseselect.dto.RecommendResponse;
import com.courseselect.dto.SelectionDTO;
import com.courseselect.service.RecommendationService;
import com.courseselect.service.SelectionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/selection")
public class SelectionController {

    private final SelectionService selectionService;
    private final RecommendationService recommendationService;

    public SelectionController(SelectionService selectionService,
                               RecommendationService recommendationService) {
        this.selectionService = selectionService;
        this.recommendationService = recommendationService;
    }

    /**
     * AI 智能推荐课程
     * POST /api/selection/recommend
     */
    @PostMapping("/recommend")
    public ApiResponse<RecommendResponse> recommend(@Valid @RequestBody RecommendRequest request) {
        RecommendResponse result = recommendationService.recommend(
                request.getStudentNo(),
                request.getSemester(),
                request.getLimit());
        return ApiResponse.ok(result);
    }

    /**
     * 学生选课
     * POST /api/selection/select?studentNo=xxx&courseCode=xxx
     */
    @PostMapping("/select")
    public ApiResponse<SelectionDTO> select(@RequestParam String studentNo,
                                             @RequestParam String courseCode) {
        SelectionDTO dto = selectionService.selectCourse(studentNo, courseCode);
        return ApiResponse.ok("选课成功", dto);
    }

    /**
     * 退选课程
     * POST /api/selection/drop?studentNo=xxx&courseCode=xxx
     */
    @PostMapping("/drop")
    public ApiResponse<Void> drop(@RequestParam String studentNo,
                                   @RequestParam String courseCode) {
        selectionService.dropCourse(studentNo, courseCode);
        return ApiResponse.ok("退选成功", null);
    }

    /**
     * 查询学生选课列表
     * GET /api/selection/list?studentNo=xxx
     */
    @GetMapping("/list")
    public ApiResponse<List<SelectionDTO>> list(@RequestParam String studentNo) {
        List<SelectionDTO> dtos = selectionService.findByStudent(studentNo);
        return ApiResponse.ok(dtos);
    }
}
