package com.xixi.controller;

import com.xixi.pojo.dto.ExamQuestionBatchImportDto;
import com.xixi.pojo.dto.ExamQuestionCreateDto;
import com.xixi.pojo.dto.ExamQuestionUpdateDto;
import com.xixi.pojo.vo.ExamQuestionVo;
import com.xixi.service.ExamService;
import com.xixi.support.CurrentTeacherResolver;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 测验题目控制器。
 */
@RestController
@RequestMapping("/study/examQuestion")
@RequiredArgsConstructor
public class ExamQuestionController {

    private final ExamService examService;
    private final CurrentTeacherResolver currentTeacherResolver;

    /**
     * 添加题目。
     * @param dto 题目参数
     * @return 结果
     */
    @PostMapping("/add")
    public Result addExamQuestion(@RequestBody ExamQuestionCreateDto dto) {
        dto.setTeacherId(currentTeacherResolver.requireCurrentTeacherId());
        return examService.addExamQuestion(dto);
    }

    /**
     * 编辑题目。
     * @param dto 题目参数
     * @return 结果
     */
    @PutMapping("/update")
    public Result updateExamQuestion(@RequestBody ExamQuestionUpdateDto dto) {
        dto.setTeacherId(currentTeacherResolver.requireCurrentTeacherId());
        return examService.updateExamQuestion(dto);
    }

    /**
     * 删除题目。
     * @param questionId 题目ID
     * @param teacherId 教师ID
     * @return 结果
     */
    @DeleteMapping("/delete/{questionId}")
    public Result deleteExamQuestion(@PathVariable Long questionId) {
        return examService.deleteExamQuestion(questionId, currentTeacherResolver.requireCurrentTeacherId());
    }

    /**
     * 批量导入题目。
     * @param dto 导入参数
     * @return 结果
     */
    @PostMapping("/batchImport")
    public Result batchImportExamQuestion(@RequestBody ExamQuestionBatchImportDto dto) {
        dto.setTeacherId(currentTeacherResolver.requireCurrentTeacherId());
        return examService.batchImportExamQuestion(dto);
    }

    /**
     * 查看题目列表。
     * 教师查看时返回含正确答案的数据；学生查看时返回脱敏数据。
     * @param examId 测验ID
     * @return 题目列表
     */
    @GetMapping("/list/{examId}")
    public Result getExamQuestionList(@PathVariable Long examId) {
        List<ExamQuestionVo> list = examService.getExamQuestionList(examId, currentTeacherResolver.requireCurrentTeacherId());
        return Result.success(list);
    }
}
