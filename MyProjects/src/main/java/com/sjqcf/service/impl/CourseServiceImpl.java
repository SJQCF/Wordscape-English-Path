package com.sjqcf.service.impl;

import com.github.pagehelper.PageHelper;
import com.sjqcf.mapper.CourseMapper;
import com.sjqcf.pojo.Course;
import com.sjqcf.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.sjqcf.utils.SystemConstants.PAGE_SIZE;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Override
    public List<Course> getCourses(Integer pageNum) {
        PageHelper.startPage(pageNum,PAGE_SIZE);
        return courseMapper.getCourses();
    }

    @Override
    public List<Course> getCoursesByName(String name, Integer pageNum) {
        PageHelper.startPage(pageNum,PAGE_SIZE);
        return courseMapper.getCoursesByName(name);
    }

    @Override
    public List<Integer> getCoursesByUserId(Integer userId, Integer pageNum) {
        return courseMapper.getCoursesByUserId(userId);
    }

    @Override
    public List<Course> getCourseById(List<Integer> ids) {
        return courseMapper.getCourseById(ids);
    }

    @Override
    public void starCourse(Integer userId, Integer courseId) {
        courseMapper.starCourse(userId, courseId);
    }

    @Override
    public void unstarCourse(Integer userId, Integer courseId) {
        courseMapper.unstarCourse(userId, courseId);
    }

    @Override
    public Course findCourseById(Integer courseId) {
        return courseMapper.findCourseById(courseId);
    }

    @Override
    public boolean checkFavorite(Integer userId, Integer courseId) {
        return courseMapper.checkFavorite(userId, courseId) > 0;
    }

    @Override
    public void buyCourse(Integer userId, Integer courseId) {
        courseMapper.buyCourse(userId, courseId);
    }

    @Override
    public List<Course> getBoughtCoursesByUserId(Integer userId, Integer pageNum) {
        PageHelper.startPage(pageNum,PAGE_SIZE);
        return courseMapper.getCourseById(courseMapper.getBoughtCoursesByUserId(userId));
    }

    @Override
    public boolean checkBoughtCourse(Integer userId, Integer courseId) {
        return courseMapper.checkBoughtCourse(userId, courseId) > 0;
    }

    @Override
    public String getCourseVideo(Integer courseId) {
        return courseMapper.getCourseVideo(courseId);
    }
}
