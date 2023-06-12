package info.touret.bookstore.spring.book.repository;
import info.touret.bookstore.spring.book.entity.Book;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Book Spring Data Repository
 */
public interface BookRepository extends CrudRepository<Book, Long> {

    @Query(value = "select b.id from Book b")
    List<Long> findAllIds();
}
