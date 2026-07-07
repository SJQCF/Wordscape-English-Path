package com.sjqcf.controller;

import com.sjqcf.pojo.Result;
import com.sjqcf.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    /**
     * 查询全部课程
     */
    @GetMapping("/courses")
    public Result getCourses(Integer pageNum) {
        log.info("查询全部课程");
        return Result.success(courseService.getCourses(pageNum));
    }

    /**
     * 根据名字查询课程
     */
    @GetMapping("/coursesByName")
    public Result getCoursesByName(String name, Integer pageNum) {
        log.info("根据名字{}查询课程", name);
        return Result.success(courseService.getCoursesByName(name, pageNum));
    }

    /**
     * 查询用户收藏的课程
     */
    @GetMapping("/favoriteCoursesByUserId/{userId}")
    public Result getCoursesByUserId(@PathVariable Integer userId, Integer pageNum) {
        log.info("查询用户{}收藏的课程", userId);
        List<Integer> ids = courseService.getCoursesByUserId(userId, pageNum);
        return Result.success(courseService.getCourseById(ids));
    }

    /**
     * 根据id查询课程列表
     */
    @GetMapping("/courseById")
    public Result getCourseById(List<Integer> courseId) {
        log.info("根据id查询课程列表{}", courseId);
        return Result.success(courseService.getCourseById(courseId));
    }

    /**
     * 收藏课程
     */
    @PostMapping("/favoriteCourse/{userId}")
    public Result starCourse(@PathVariable Integer userId, @RequestParam Integer courseId) {
        log.info("用户{}收藏课程{}", userId, courseId);
        courseService.starCourse(userId, courseId);
        return Result.success();
    }

    /**
     * 取消收藏课程
     */
    @PostMapping("/cancelFavoriteCourse/{userId}")
    public Result unstarCourse(@PathVariable Integer userId, @RequestParam Integer courseId) {
        log.info("用户{}取消收藏课程{}", userId, courseId);
        courseService.unstarCourse(userId, courseId);
        return Result.success();
    }

    /**
     * 根据id查询课程
     */
    @GetMapping("/getCourseById")
    public Result getCourseById(@RequestParam Integer courseId) {
        log.info("根据id查询课程{}", courseId);
        return Result.success(courseService.findCourseById(courseId));
    }

    /**
     * 检查用户是否收藏过此课程
     */
    @GetMapping("/checkFavoriteCourse/{userId}")
    public Result checkFavoriteCourse(@PathVariable Integer userId, @RequestParam Integer courseId) {
        log.info("检查用户{}是否收藏过课程{}", userId, courseId);
        boolean isFavorite = courseService.checkFavorite(userId, courseId);
        return Result.success(isFavorite);
    }

    /**
     * 用户购买课程
     */
    @PostMapping("/buyCourse/{userId}")
    public Result buyCourse(@PathVariable Integer userId, @RequestParam Integer courseId) {
        log.info("用户{}购买课程{}", userId, courseId);
        courseService.buyCourse(userId, courseId);
        return Result.success();
    }

    /**
     * 获取用户已购买的课程
     */
    @GetMapping("/getBoughtCourse/{userId}")
    public Result getBoughtCourse(@PathVariable Integer userId, @RequestParam Integer pageNum) {
        log.info("获取用户{}已购买的课程", userId);
        return Result.success(courseService.getBoughtCoursesByUserId(userId, pageNum));
    }

    /**
     * 检查用户是否已购买此课程
     */
    @GetMapping("/checkBoughtCourse/{userId}")
    public Result checkBoughtCourse(@PathVariable Integer userId, @RequestParam Integer courseId) {
        log.info("检查用户{}是否已购买课程{}", userId, courseId);
        boolean isBought = courseService.checkBoughtCourse(userId, courseId);
        return Result.success(isBought);
    }

    /**
     * 查询课程视频
     */
    @GetMapping("/getCourseVideo")
    public Result getCourseVideo(Integer courseId) {
        log.info("查询课程{}视频", courseId);
        return Result.success(courseService.getCourseVideo(courseId));
    }
}
