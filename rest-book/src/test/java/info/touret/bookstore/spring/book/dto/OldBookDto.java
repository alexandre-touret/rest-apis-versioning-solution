package info.touret.bookstore.spring.book.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * BookDto
 */

@JsonTypeName("Book")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-06-05T19:19:38.239576481+02:00[Europe/Paris]")
public class OldBookDto {

  private String title;

  private String isbn13;

  private String isbn10;

  private String author;

  private Integer yearOfPublication;

  private Integer nbOfPages;

  private Integer rank;

  private BigDecimal price;

  private String smallImageUrl;

  private String mediumImageUrl;

  private String description;

  private Long id;

  /**
   * Default constructor
   *
   * @deprecated Use {@link OldBookDto#OldBookDto(String)}
   */
  @Deprecated
  public OldBookDto() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public OldBookDto(String title) {
    this.title = title;
  }

  public OldBookDto title(String title) {
    this.title = title;
    return this;
  }

  /**
   * Get title
   *
   * @return title
   */
  @NotNull
  @Schema(name = "title", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public OldBookDto isbn13(String isbn13) {
    this.isbn13 = isbn13;
    return this;
  }

  /**
   * Get isbn13
   *
   * @return isbn13
   */

  @Schema(name = "isbn13", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("isbn13")
  public String getIsbn13() {
    return isbn13;
  }

  public void setIsbn13(String isbn13) {
    this.isbn13 = isbn13;
  }

  public OldBookDto isbn10(String isbn10) {
    this.isbn10 = isbn10;
    return this;
  }

  /**
   * Get isbn10
   *
   * @return isbn10
   */

  @Schema(name = "isbn10", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("isbn10")
  public String getIsbn10() {
    return isbn10;
  }

  public void setIsbn10(String isbn10) {
    this.isbn10 = isbn10;
  }

  public OldBookDto author(String author) {
    this.author = author;
    return this;
  }

  /**
   * Get author
   *
   * @return author
   */

  @Schema(name = "author", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("author")
  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public OldBookDto yearOfPublication(Integer yearOfPublication) {
    this.yearOfPublication = yearOfPublication;
    return this;
  }

  /**
   * Get yearOfPublication
   *
   * @return yearOfPublication
   */

  @Schema(name = "yearOfPublication", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("yearOfPublication")
  public Integer getYearOfPublication() {
    return yearOfPublication;
  }

  public void setYearOfPublication(Integer yearOfPublication) {
    this.yearOfPublication = yearOfPublication;
  }

  public OldBookDto nbOfPages(Integer nbOfPages) {
    this.nbOfPages = nbOfPages;
    return this;
  }

  /**
   * Get nbOfPages
   *
   * @return nbOfPages
   */

  @Schema(name = "nbOfPages", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("nbOfPages")
  public Integer getNbOfPages() {
    return nbOfPages;
  }

  public void setNbOfPages(Integer nbOfPages) {
    this.nbOfPages = nbOfPages;
  }

  public OldBookDto rank(Integer rank) {
    this.rank = rank;
    return this;
  }

  /**
   * Get rank
   * minimum: 1
   * maximum: 10
   *
   * @return rank
   */
  @Min(1)
  @Max(10)
  @Schema(name = "rank", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("rank")
  public Integer getRank() {
    return rank;
  }

  public void setRank(Integer rank) {
    this.rank = rank;
  }

  public OldBookDto price(BigDecimal price) {
    this.price = price;
    return this;
  }

  /**
   * Get price
   *
   * @return price
   */
  @Valid
  @Schema(name = "price", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("price")
  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public OldBookDto smallImageUrl(String smallImageUrl) {
    this.smallImageUrl = smallImageUrl;
    return this;
  }

  /**
   * Get smallImageUrl
   *
   * @return smallImageUrl
   */

  @Schema(name = "smallImageUrl", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("smallImageUrl")
  public String getSmallImageUrl() {
    return smallImageUrl;
  }

  public void setSmallImageUrl(String smallImageUrl) {
    this.smallImageUrl = smallImageUrl;
  }

  public OldBookDto mediumImageUrl(String mediumImageUrl) {
    this.mediumImageUrl = mediumImageUrl;
    return this;
  }

  /**
   * Get mediumImageUrl
   *
   * @return mediumImageUrl
   */

  @Schema(name = "mediumImageUrl", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("mediumImageUrl")
  public String getMediumImageUrl() {
    return mediumImageUrl;
  }

  public void setMediumImageUrl(String mediumImageUrl) {
    this.mediumImageUrl = mediumImageUrl;
  }

  public OldBookDto description(String description) {
    this.description = description;
    return this;
  }

  /**
   * Get description
   *
   * @return description
   */

  @Schema(name = "description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public OldBookDto id(Long id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   *
   * @return id
   */

  @Schema(name = "id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("id")
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OldBookDto book = (OldBookDto) o;
    return Objects.equals(this.title, book.title) &&
            Objects.equals(this.isbn13, book.isbn13) &&
            Objects.equals(this.isbn10, book.isbn10) &&
            Objects.equals(this.author, book.author) &&
            Objects.equals(this.yearOfPublication, book.yearOfPublication) &&
            Objects.equals(this.nbOfPages, book.nbOfPages) &&
            Objects.equals(this.rank, book.rank) &&
            Objects.equals(this.price, book.price) &&
            Objects.equals(this.smallImageUrl, book.smallImageUrl) &&
            Objects.equals(this.mediumImageUrl, book.mediumImageUrl) &&
            Objects.equals(this.description, book.description) &&
            Objects.equals(this.id, book.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, isbn13, isbn10, author, yearOfPublication, nbOfPages, rank, price, smallImageUrl, mediumImageUrl, description, id);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OldBookDto {\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    isbn13: ").append(toIndentedString(isbn13)).append("\n");
    sb.append("    isbn10: ").append(toIndentedString(isbn10)).append("\n");
    sb.append("    author: ").append(toIndentedString(author)).append("\n");
    sb.append("    yearOfPublication: ").append(toIndentedString(yearOfPublication)).append("\n");
    sb.append("    nbOfPages: ").append(toIndentedString(nbOfPages)).append("\n");
    sb.append("    rank: ").append(toIndentedString(rank)).append("\n");
    sb.append("    price: ").append(toIndentedString(price)).append("\n");
    sb.append("    smallImageUrl: ").append(toIndentedString(smallImageUrl)).append("\n");
    sb.append("    mediumImageUrl: ").append(toIndentedString(mediumImageUrl)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

