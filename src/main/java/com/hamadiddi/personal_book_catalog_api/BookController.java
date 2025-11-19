package com.hamadiddi.personal_book_catalog_api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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

        Map<String, Object> resp = new HashMap<>();

        // Step 1: Application-level validation (fast path)
        Optional<Book> bookOptional = bookRepo.findByNameIgnoreCase(bookReqDto.getName());
        if (bookOptional.isPresent()) {
            resp.put("message", "The book " + bookReqDto.getName() + " is already present");
            resp.put("status", "fail");
            resp.put("data", null);
            resp.put("code", 409);
            return ResponseEntity.ok(resp);
        }

        // Step 2: Attempt to persist - DB-level constraint enforces final check
        try {
            Book book = Book.builder()
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

        } catch (DataIntegrityViolationException e) {
            // Triggered by DB-level unique constraint (LOWER(name) UNIQUE)
            resp.put("message", "The book " + bookReqDto.getName() + " is already present");
            resp.put("status", "fail");
            resp.put("data", null);
            resp.put("code", 409);
            return ResponseEntity.status(409).body(resp);
        }
    }


    @GetMapping("/")
    public ResponseEntity<?> getAllBooks() {
        List<Book> books = bookRepo.findAll();
        return ResponseEntity.ok(books);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@RequestBody BookReqDto bookReqDto, @PathVariable Long id) {
        Optional<Book> book = bookRepo.findByNameAndId(bookReqDto.getName(), id);
        Map<String, Object> resp = new HashMap<>();
        if (book.isPresent()) {
            resp.put("message", "The book name " + bookReqDto.getName() + " is already present");
            resp.put("status", "fail");
            resp.put("data", null);
            resp.put("code", 409);
            return ResponseEntity.ok(resp);
        }
        Optional<Book> book2 = bookRepo.findById(id);
        if (book2.isEmpty()) {
            resp.put("message", "The book with the id " + id + " does not exist");
            resp.put("status", "fail");
            resp.put("data", null);
            resp.put("code", 404);
            return ResponseEntity.ok(resp);
        }
        Book bookFinal = book2.get();
        bookFinal.setName(bookReqDto.getName());
        bookFinal.setAuthor(bookReqDto.getAuthor());
        bookFinal.setGenre(bookReqDto.getGenre());

        Book bookSaved = bookRepo.save(bookFinal);

        resp.put("message", "The book is updated successfully");
        resp.put("status", "success");
        resp.put("data", bookSaved);
        resp.put("code", 200);
        return ResponseEntity.ok(resp);
    }
}
