package com.sjqcf.controller;

import com.sjqcf.pojo.Result;
import com.sjqcf.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/book")
public class BookController {

    @Autowired
    private BookService bookService;

    /**
     * 获取图书列表
     */
    @GetMapping("/getBooks")
    public Result getBook(Integer pageNum) {
        log.info("获取图书列表");
        return Result.success(bookService.getBooks(pageNum));
    }

    /**
     * 根据名字获取图书
     */
    @GetMapping("/getBookByName")
    public Result getBookByName(String name, Integer pageNum) {
        log.info("获取图书列表");
        return Result.success(bookService.getBooksByName(name, pageNum));
    }

    /**
     * 根据类型获取图书
     */
    @GetMapping("/getBookByCategory")
    public Result getBookByCategory(Integer category, Integer pageNum) {
        log.info("获取图书列表");
        return Result.success(bookService.getBooksByCategory(category, pageNum));
    }

    /**
     * 根据id获取图书
     */
    @GetMapping("/getBookById/{bookId}")
    public Result getBookById(@PathVariable Integer bookId) {
        log.info("根据id获取图书: bookId={}", bookId);
        return Result.success(bookService.getBookById(bookId));
    }

    /**
     * 用户收藏图书
     */
    @PostMapping("/favoriteBook/{userId}")
    public Result favoriteBook(@PathVariable Integer userId, @RequestParam Integer bookId) {
        log.info("用户收藏图书: userId={}, bookId={}", userId, bookId);
        bookService.starBook(userId, bookId);
        return Result.success();
    }

    /**
     * 取消收藏图书
     */
    @PostMapping("/cancelFavoriteBook/{userId}")
    public Result cancelFavoriteBook(@PathVariable Integer userId, @RequestParam Integer bookId) {
        log.info("取消收藏图书: userId={}, bookId={}", userId, bookId);
        bookService.cancelStarBook(userId, bookId);
        return Result.success();
    }

    /**
     * 获取用户收藏的图书
     */
    @GetMapping("/getFavoriteBooks/{userId}")
    public Result getFavoriteBooks(@PathVariable Integer userId) {
        log.info("获取用户收藏的图书: userId={}", userId);
        return Result.success(bookService.getStarBooks(userId));
    }

    /**
     * 检查用户是否收藏过此图书
     */
    @GetMapping("/checkFavoriteBook/{userId}")
    public Result checkFavoriteBook(@PathVariable Integer userId, @RequestParam Integer bookId) {
        log.info("检查用户是否收藏过此图书: userId={}, bookId={}", userId, bookId);
        return Result.success(bookService.checkStarBook(userId, bookId));
    }
}
