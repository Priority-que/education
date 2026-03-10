package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.TeachersDto;
import com.xixi.pojo.query.TeachersQuery;
import com.xixi.pojo.vo.TeachersVo;
import com.xixi.service.TeachersService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeachersController {
    private final TeachersService teachersService;
    
    @GetMapping("/getPage")
    public Result getPage(TeachersQuery teachersQuery) {
        IPage<TeachersVo> page = teachersService.getPage(teachersQuery);
        return Result.success(page);
    }
    
    @GetMapping("getTeacherById/{id}")
    public Result getTeacherById(@PathVariable Long id) {
        TeachersVo teacher = teachersService.getTeacherById(id);
        return Result.success(teacher);
    }

    @GetMapping("/getTeachersIdByName")
    public Result getTeachersIdByName(String name) {
        return teachersService.getTeachersIdByName(name);
    }
    @GetMapping("/getTeachersNameById/{id}")
    public Result getTeachersNameById(@PathVariable Long id) {
        return teachersService.getTeachersNameById(id);
    }

    @GetMapping("/getTeacherIdByUserId/{userId}")
    public Result getTeacherIdByUserId(@PathVariable Long userId) {
        return teachersService.getTeacherIdByUserId(userId);
    }

    @PostMapping("/addTeacher")
    public Result add(TeachersDto teachersDto) {
        return teachersService.addTeacher(teachersDto);
    }
    
    @PostMapping("/updateTeacher")
    public Result update(TeachersDto teachersDto) {
        return teachersService.updateTeacher(teachersDto);
    }
    
    @PostMapping("/deleteTeacher")
    public Result delete(@RequestParam List<Long> ids) {
        return teachersService.deleteTeacher(ids);
    }

}
