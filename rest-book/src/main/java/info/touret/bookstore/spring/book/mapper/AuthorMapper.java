package info.touret.bookstore.spring.book.mapper;

import info.touret.bookstore.spring.book.entity.Author;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface AuthorMapper {

    default String toString(List<Author> authors) {
        if (authors != null && authors.get(0) != null) {
            return authors.get(0).getFirstname() + " " + authors.get(0).getLastname();
        }
        return null;
    }

    default List<Author> toAuthorList(String author) {
        if (author != null) {
            Author newAuthor = new Author();
            newAuthor.setLastname(author);
            return List.of(newAuthor);
        }
        return null;
    }

}
