package com.sjqcf.service;

import com.sjqcf.pojo.Book;

import java.util.List;

public interface BookService {
    List<Book> getBooks(Integer pageNum);

    List<Book> getBooksByName(String name, Integer pageNum);

    List<Book> getBooksByCategory(Integer category, Integer pageNum);

    Book getBookById(Integer id);

    void starBook(Integer userId, Integer bookId);

    List<Book> getStarBooks(Integer userId);

    void cancelStarBook(Integer userId, Integer bookId);

    boolean checkStarBook(Integer userId, Integer bookId);
}
