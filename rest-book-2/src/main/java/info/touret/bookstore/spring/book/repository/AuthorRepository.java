package info.touret.bookstore.spring.book.repository;

import info.touret.bookstore.spring.book.entity.Author;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthorRepository extends CrudRepository<Author,Long> {
    Optional<Author> findByPublicId(UUID uuid);

}
