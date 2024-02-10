/*
package ru.polescanner.books

import com.example.app.domain.books.Book
import com.example.app.domain.IMapper
import ru.polescanner.ksp.MapTo

data class BookDTO(val isbn: String, val name: String, val pages: Int) {
    @MapTo
    object Mapper: IMapper<Book, BookDTO> {
        override fun mapTo(i: Book) = BookDTO(i.isbn, i.name, i.pages)

        override fun mapFrom(o: BookDTO) = Book(o.isbn, o.name, o.pages)
    }
}*/
