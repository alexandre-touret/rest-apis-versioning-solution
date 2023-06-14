package info.touret.bookstore.spring.book.mapper;

import info.touret.bookstore.spring.book.entity.Book;
import info.touret.bookstore.spring.book.generated.dto.BookDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface BookMapper {
    Book toBook(BookDto bookDto);

    BookDto toBookDto(Book book);

    List<BookDto> toBookDtos(List<Book> books);
}
