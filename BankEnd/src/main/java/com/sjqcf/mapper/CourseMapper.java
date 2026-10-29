package com.sjqcf.mapper;

import com.sjqcf.pojo.Course;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CourseMapper {

    List<Course> getCourses();

    List<Course> getCoursesByName(String name);

    List<Course> getCoursesByDescription(String description);

    List<Integer> getCoursesByUserId(Integer userId);

    List<Course> getCourseById(List<Integer> ids);

    void starCourse(Integer userId, Integer courseId);

    void unstarCourse(Integer userId, Integer courseId);

    Course findCourseById(Integer courseId);

    Integer checkFavorite(Integer userId, Integer courseId);

    void buyCourse(Integer userId, Integer courseId);

    List<Integer> getBoughtCoursesByUserId(Integer userId);

    Integer checkBoughtCourse(Integer userId, Integer courseId);

    String getCourseVideo(Integer courseId);
}
