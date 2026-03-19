package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.StudentsDto;
import com.xixi.pojo.query.StudentsQuery;
import com.xixi.pojo.vo.StudentsVo;
import com.xixi.service.StudentsService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentsController {
    private final StudentsService studentsService;
    
    @GetMapping("/getPage")
    public Result getPage(StudentsQuery studentsQuery) {
        IPage<StudentsVo> page = studentsService.getPage(studentsQuery);
        return Result.success(page);
    }
    
    @GetMapping("/getStudentById/{id}")
    public Result getStudentById(@PathVariable Long id) {
        StudentsVo student = studentsService.getStudentById(id);
        return Result.success(student);
    }

    @GetMapping("/getStudentByUserId/{userId}")
    public Result getStudentByUserId(@PathVariable Long userId) {
        StudentsVo student = studentsService.getStudentByUserId(userId);
        return Result.success(student);
    }
    
    @PostMapping("/addStudent")
    public Result addStudent(@RequestBody StudentsDto studentsDto) {
        return studentsService.addStudent(studentsDto);
    }
    
    @PostMapping("/updateStudent")
    public Result updateStudent(@RequestBody StudentsDto studentsDto) {
        return studentsService.updateStudent(studentsDto);
    }
    
    @PostMapping("/deleteStudent")
    public Result deleteStudent(@RequestParam List<Long> ids) {
        return studentsService.deleteStudent(ids);
    }
}
