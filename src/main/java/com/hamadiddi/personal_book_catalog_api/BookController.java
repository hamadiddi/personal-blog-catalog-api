package com.hamadiddi.personal_book_catalog_api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookRepo bookRepo;

//    @GetMapping("/")
//    public List<Book> getBook() {
//        return bookRepo.findAll();
//    }

    @PostMapping("/book")
    public ResponseEntity<?> addBook(@RequestBody BookReqDto bookReqDto) {
        Optional<Book> bookOptional = bookRepo.findByName(bookReqDto.getName());
        Map<String, Object> resp = new HashMap<>();

        if (bookOptional.isPresent()) {
            resp.put("message", "The book " + bookReqDto.getName() + " is already present");
            resp.put("status", "fail");
            resp.put("data", null);
            resp.put("code", 409);
            return ResponseEntity.ok(resp);
        }
        var book = Book.builder()
                .name(bookReqDto.getName())
                .author(bookReqDto.getAuthor())
                .genre(bookReqDto.getGenre())
                .build();
        Book savedBook = bookRepo.save(book);
        resp.put("message", "The book was added successfully");
        resp.put("status", "success");
        resp.put("data", savedBook);
        resp.put("code", 200);

        return ResponseEntity.ok(resp);
    }
}
