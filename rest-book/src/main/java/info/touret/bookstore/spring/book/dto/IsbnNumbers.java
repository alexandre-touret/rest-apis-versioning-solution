package info.touret.bookstore.spring.book.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

/**
 * Exposes only the useful attributes from the payload returned by Numbers API
 */
@JsonTypeName("BookNumbers")
public class IsbnNumbers implements Serializable {
    @JsonProperty("isbn_10")
    private String isbn10;

    @JsonProperty("isbn_13")
    private String isbn13;

    public String getIsbn10() {
        return isbn10;
    }

    public void setIsbn10(String isbn10) {
        this.isbn10 = isbn10;
    }

    public String getIsbn13() {
        return isbn13;
    }

    public void setIsbn13(String isbn13) {
        this.isbn13 = isbn13;
    }
}
