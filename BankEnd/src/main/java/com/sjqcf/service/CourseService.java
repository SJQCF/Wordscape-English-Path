package com.sjqcf.service;

import com.sjqcf.pojo.Course;

import java.util.List;

public interface CourseService {
    List<Course> getCourses(Integer pageNum);

    List<Course> getCoursesByName(String name, Integer pageNum);

    List<Course> getCoursesByDescription(String description, Integer pageNum);

    List<Integer> getCoursesByUserId(Integer userId, Integer pageNum);

    List<Course> getCourseById(List<Integer> ids);

    void starCourse(Integer userId, Integer courseId);

    void unstarCourse(Integer userId, Integer courseId);

    Course findCourseById(Integer courseId);

    boolean checkFavorite(Integer userId, Integer courseId);

    void buyCourse(Integer userId, Integer courseId);

    List<Course> getBoughtCoursesByUserId(Integer userId, Integer pageNum);

    boolean checkBoughtCourse(Integer userId, Integer courseId);

    String getCourseVideo(Integer courseId);
}
