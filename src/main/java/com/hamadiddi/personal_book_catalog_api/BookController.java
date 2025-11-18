package com.hamadiddi.personal_book_catalog_api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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

    @GetMapping("/{id}")
    public ResponseEntity<?> getAbook(@PathVariable Long id) {
        Map<String, Object> resp = new HashMap<>();
        Optional<Book> book = bookRepo.findById(id);
        if (book.isEmpty()) {
            resp.put("message", "The book with the id " + id + " is not available atm");
            resp.put("status", "fail");
            resp.put("data", null);
            resp.put("code", 404);
            return ResponseEntity.ok(resp);
        }
        return ResponseEntity.ok(book);
    }


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

    @GetMapping("/")
    public ResponseEntity<?> getAllBooks() {
        List<Book> books = bookRepo.findAll();
        return ResponseEntity.ok(books);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@RequestBody BookReqDto bookReqDto, @PathVariable Long id) {
        Map<String, Object> resp = new HashMap<>();

        // 1) Check if another book (other than this id) has the same name (case-insensitive)
        Optional<Book> existing = bookRepo.findByNameIgnoreCase(bookReqDto.getName());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            resp.put("message", "Book name '" + bookReqDto.getName() + "' already exists");
            resp.put("status", "fail");
            resp.put("code", 409);
            resp.put("data", null);
            return ResponseEntity.status(409).body(resp);
        }

        // 2) Check if this book exists
        Optional<Book> optionalBook = bookRepo.findById(id);
        if (optionalBook.isEmpty()) {
            resp.put("message", "Book with id " + id + " not found");
            resp.put("status", "fail");
            resp.put("code", 404);
            resp.put("data", null);
            return ResponseEntity.status(404).body(resp);
        }

        Book book = optionalBook.get();
        book.setName(bookReqDto.getName());
        book.setAuthor(bookReqDto.getAuthor());
        book.setGenre(bookReqDto.getGenre());

        //using try-catch to handle multi threading
        try {
            Book saved = bookRepo.save(book);

            resp.put("message", "Book updated successfully");
            resp.put("status", "success");
            resp.put("code", 200);
            resp.put("data", saved);
            return ResponseEntity.ok(resp);

        } catch (DataIntegrityViolationException e) {
            // DB fallback: functional index lower(name) prevented duplicate
            resp.put("message", "Book name '" + bookReqDto.getName() + "' already exists");
            resp.put("status", "fail");
            resp.put("code", 409);
            resp.put("data", null);
            return ResponseEntity.status(409).body(resp);
        }
    }
}
