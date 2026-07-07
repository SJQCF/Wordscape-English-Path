package com.sjqcf.mapper;

import com.sjqcf.pojo.Book;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BookMapper {
    public List<Book> getBooks();

    public Book findById(Integer id);

    public List<Book> findByCategory(Integer category);

    public List<Book> findByName(String name);

    void starBook(Integer userId, Integer bookId);

    List<Book> findUserStarBooks(Integer userId);

    void cancelStarBook(Integer userId, Integer bookId);

    Integer checkStarBook(Integer userId, Integer bookId);
}
