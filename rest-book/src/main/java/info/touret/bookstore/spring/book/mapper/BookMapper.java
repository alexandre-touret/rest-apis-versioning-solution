package info.touret.bookstore.spring.book.mapper;

import info.touret.bookstore.spring.book.entity.Book;
import info.touret.bookstore.spring.book.generated.dto.BookDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(uses = AuthorMapper.class)
public interface BookMapper {
    @Mapping(source = "author",target = "authors")
    Book toBook(BookDto bookDto);

    @Mapping(source = "authors",target = "author")
    BookDto toBookDto(Book book);

    List<BookDto> toBookDtos(List<Book> books);
}
