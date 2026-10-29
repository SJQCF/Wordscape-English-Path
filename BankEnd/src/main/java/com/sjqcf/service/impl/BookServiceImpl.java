package com.sjqcf.service.impl;

import com.github.pagehelper.PageHelper;
import com.sjqcf.mapper.BookMapper;
import com.sjqcf.pojo.Book;
import com.sjqcf.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.sjqcf.utils.SystemConstants.PAGE_SIZE;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookMapper bookMapper;

    @Override
    public List<Book> getBooks(Integer pageNum) {
        PageHelper.startPage(pageNum,PAGE_SIZE);
        return bookMapper.getBooks();
    }

    @Override
    public List<Book> getBooksByName(String name, Integer pageNum) {
        PageHelper.startPage(pageNum,PAGE_SIZE);
        return bookMapper.findByName(name);
    }

    @Override
    public List<Book> getBooksByCategory(Integer category, Integer pageNum) {
        PageHelper.startPage(pageNum,PAGE_SIZE);
        return bookMapper.findByCategory(category);
    }

    @Override
    public Book getBookById(Integer id) {
        return bookMapper.findById(id);
    }

    @Override
    public void starBook(Integer userId, Integer bookId) {
        bookMapper.starBook(userId,bookId);
    }

    @Override
    public List<Book> getStarBooks(Integer userId) {
        return bookMapper.findUserStarBooks(userId);
    }

    @Override
    public void cancelStarBook(Integer userId, Integer bookId) {
        bookMapper.cancelStarBook(userId,bookId);
    }

    @Override
    public boolean checkStarBook(Integer userId, Integer bookId) {
        return bookMapper.checkStarBook(userId,bookId) > 0;
    }
}
