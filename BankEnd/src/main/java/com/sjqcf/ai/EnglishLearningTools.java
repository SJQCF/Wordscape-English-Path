package com.sjqcf.ai;

import com.sjqcf.pojo.Audio;
import com.sjqcf.pojo.Book;
import com.sjqcf.pojo.Course;
import com.sjqcf.pojo.Word;
import com.sjqcf.service.BookService;
import com.sjqcf.service.CourseService;
import com.sjqcf.service.ListeningService;
import com.sjqcf.service.WordService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class EnglishLearningTools {

    private static final int MAX_RESULTS = 8;

    private final WordService wordService;
    private final CourseService courseService;
    private final ListeningService listeningService;
    private final BookService bookService;

    public EnglishLearningTools(WordService wordService,
                                CourseService courseService,
                                ListeningService listeningService,
                                BookService bookService) {
        this.wordService = wordService;
        this.courseService = courseService;
        this.listeningService = listeningService;
        this.bookService = bookService;
    }

    @Tool(name = "searchVocabulary",
            description = "查询系统词库中的单词。适合查英文拼写、音标、中文释义和例句。")
    public List<Word> searchVocabulary(
            @ToolParam(description = "英文单词、音标片段或中文释义关键词") String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        List<Word> words = wordService.find(keyword.trim(), 1);
        if (words.isEmpty()) {
            words = wordService.findByCn(keyword.trim(), 1);
        }
        return words.stream().limit(MAX_RESULTS).toList();
    }

    @Tool(name = "searchCourses",
            description = """
                    按课程名称关键词查询英语课程，返回课程名称、教师、时长、价格和简介。
                    关键词为空时返回推荐课程列表。
                    """)
    public List<Course> searchCourses(
            @ToolParam(required = false, description = "课程名称关键词，例如四级、听力、写作") String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return courseService.getCourses(1).stream().limit(MAX_RESULTS).toList();
        }

        // 先按课程名在数据库里做模糊匹配，覆盖全表而不只是第一页
        List<Course> courses = courseService.getCoursesByName(keyword.trim(), 1);
        if (courses.isEmpty()) {
            // 课程名没命中时，退化为对第一页的标题/描述/教师做一次宽松匹配
            String normalized = keyword.trim().toLowerCase(Locale.ROOT);
            courses = courseService.getCourses(1).stream()
                    .filter(course ->
                            contains(course.getTitle(), normalized)
                                    || contains(course.getDescription(), normalized)
                                    || contains(course.getTeacher(), normalized))
                    .toList();
        }
        return courses.stream().limit(MAX_RESULTS).toList();
    }

    @Tool(name = "searchCoursesByDescription",
            description = """
                    按课程描述内容查找相关课程。当用户表达的是学习需求或目标
                    （例如“想练听力”“备考四级”“提高写作”），说不清具体课程名时，用这个工具。
                    它会在课程简介里做关键词匹配，返回课程名称、教师、时长、价格和简介。
                    """)
    public List<Course> searchCoursesByDescription(
            @ToolParam(description = "描述学习需求或课程内容的关键词，例如听力、四级、写作") String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return courseService.getCoursesByDescription(keyword.trim(), 1).stream()
                .limit(MAX_RESULTS)
                .toList();
    }

    @Tool(name = "searchBooks",
            description = """
                    查询系统图书库中的英语读物，返回书名、作者、简介、价格和分类。
                    分类编号：1 小说，2 散文，3 学术文章，4 诗文。
                    书名关键词和分类编号可以只传一个；都不传时返回推荐图书。
                    """)
    public List<Book> searchBooks(
            @ToolParam(required = false, description = "书名关键词") String keyword,
            @ToolParam(required = false, description = "图书分类编号：1 小说，2 散文，3 学术文章，4 诗文") Integer category) {
        List<Book> books;
        if (keyword != null && !keyword.isBlank()) {
            books = bookService.getBooksByName(keyword.trim(), 1);
            // 同时给了分类时，在书名命中结果里再按分类收窄
            if (category != null) {
                books = books.stream()
                        .filter(book -> category.equals(book.getCategory()))
                        .toList();
            }
        } else if (category != null) {
            books = bookService.getBooksByCategory(category, 1);
        } else {
            books = bookService.getBooks(1);
        }
        return books.stream().limit(MAX_RESULTS).toList();
    }

    @Tool(name = "searchListening",
            description = """
                    查询听力资源。类型编号：1 高中听力，2 四级听力，3 六级听力，
                    4 新闻报道，5 英文歌曲，6 日常对话。类型不传时返回全部。
                    """)
    public List<Audio> searchListening(
            @ToolParam(required = false, description = "听力类型编号，1-6") Integer category) {
        List<Audio> audios = category == null
                ? listeningService.getListeningList(1)
                : listeningService.getListeningListByType(1, category);
        return audios.stream().limit(MAX_RESULTS).toList();
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
