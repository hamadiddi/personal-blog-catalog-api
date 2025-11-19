package com.hamadiddi.personal_book_catalog_api;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepo extends JpaRepository<Book, Long> {

    Optional<Book> findByNameIgnoreCase(String name);

    Optional<Book> findByNameAndId(String name, Long id);
    Optional<Book> findByNameIgnoreCase(String name);
}
