package com.hamadiddi.personal_book_catalog_api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookReqDto {

    private String name;
    private String author;
    private String genre;
}
