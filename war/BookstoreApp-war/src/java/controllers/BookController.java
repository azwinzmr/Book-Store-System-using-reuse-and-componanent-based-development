package controllers;

import entities.Book;
import entities.Genre;
import session.BookService;
import session.GenreService;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList; // Added for explicit ArrayList instantiation
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named
@ViewScoped
public class BookController implements Serializable {

    @EJB
    private BookService bookService;

    @EJB
    private GenreService genreService;

    private List<Book> books;
    private List<Book> availableBooks;
    private List<Genre> genres;
    private Book selectedBook;
    private Book newBook;
    private Long selectedGenreId;
    private String searchTitle;

    @PostConstruct
    public void init() {
        newBook = new Book(); // Initialize newBook for form
        loadGenres(); // Load genres first, as books depend on them
        loadBooks(); // Load all books and available books
    }

    /**
     * Loads all books and available books from the BookService.
     */
    public void loadBooks() {
        try {
            books = bookService.getAllBooks();
            availableBooks = bookService.getAvailableBooks();
            System.out.println("BookController: Loaded " + (books != null ? books.size() : 0) + " total books.");
            System.out.println("BookController: Loaded " + (availableBooks != null ? availableBooks.size() : 0) + " available books.");
        } catch (Exception e) {
            System.err.println("Error loading books in BookController: " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to load books: " + e.getMessage()));
            books = new ArrayList<>(); // Initialize to empty list on error
            availableBooks = new ArrayList<>(); // Initialize to empty list on error
        }
    }

    /**
     * Loads all genres from the GenreService.
     * Includes a fallback mechanism if genres are initially empty.
     */
    public void loadGenres() {
        try {
            genres = genreService.getAllGenres();
            System.out.println("BookController: Loaded " + (genres != null ? genres.size() : 0) + " genres.");
            
            // This block was slightly redundant, streamlined.
            // If genres are still empty after the primary load, it might indicate
            // that DataInitializationService has not run.
            if (genres == null || genres.isEmpty()) {
                System.out.println("BookController: Genres are empty. This might indicate that initial data has not been loaded. " +
                                   "Ensure DataInitializationService is manually invoked if @Startup is disabled.");
                // No need to try and reload, as getAllGenres() was just called.
                // If you need to trigger data init, it should be done explicitly
                // (e.g., via AdminController or a dedicated setup mechanism).
            }
        } catch (Exception e) {
            System.err.println("Error loading genres in BookController: " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to load genres: " + e.getMessage()));
            genres = new ArrayList<>(); // Initialize to empty list on error
        }
    }

    /**
     * Adds a new book to the database (Admin function).
     *
     * @return null to stay on the current page.
     */
    public String addBook() {
        try {
            if (!validateBook()) {
                return null; // Stay on current page if validation fails
            }

            // Set genre from selectedGenreId
            Genre genre = genreService.findById(selectedGenreId);
            if (genre == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please select a valid genre"));
                return null;
            }

            newBook.setGenre(genre);
            bookService.createBook(newBook);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Book added successfully"));

            // Reset form fields for new input
            newBook = new Book();
            selectedGenreId = null;
            loadBooks(); // Refresh book lists after adding
            return null;
        } catch (Exception e) {
            System.err.println("Error adding book: " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to add book: " + e.getMessage()));
            return null;
        }
    }

    /**
     * Updates an existing book in the database.
     *
     * @return null to stay on the current page.
     */
    public String updateBook() {
        try {
            if (selectedBook == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No book selected for update"));
                return null;
            }
            // Re-fetch genre if selectedGenreId might have changed,
            // or ensure selectedBook's genre is correctly managed
            // For now, assuming selectedBook already has its genre correctly set from UI binding
            
            bookService.updateBook(selectedBook);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Book updated successfully"));

            loadBooks(); // Refresh book lists after update
            selectedBook = null; // Clear selected book
            return null;
        } catch (Exception e) {
            System.err.println("Error updating book: " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to update book: " + e.getMessage()));
            return null;
        }
    }

    /**
     * Deletes a book from the database.
     *
     * @param book The book entity to delete.
     * @return null to stay on the current page.
     */
    public String deleteBook(Book book) {
        try {
            if (book == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No book selected for deletion"));
                return null;
            }
            bookService.deleteBook(book.getId());

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Book deleted successfully"));

            loadBooks(); // Refresh book lists after deletion
            return null;
        } catch (Exception e) {
            System.err.println("Error deleting book: " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to delete book: " + e.getMessage()));
            return null;
        }
    }

    /**
     * Searches for books by title.
     * The search is case-insensitive and supports partial matches.
     */
    public void searchBooks() {
        try {
            if (searchTitle != null && !searchTitle.trim().isEmpty()) {
                books = bookService.searchBooksByTitle(searchTitle);
            } else {
                loadBooks(); // If search term is empty, reload all books
            }
        } catch (Exception e) {
            System.err.println("Error during book search: " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Search failed: " + e.getMessage()));
            books = new ArrayList<>(); // Clear results on error
        }
    }

    /**
     * Filters the book list by the selected genre.
     */
    public void filterByGenre() {
        try {
            if (selectedGenreId != null && selectedGenreId > 0) {
                books = bookService.getBooksByGenre(selectedGenreId);
            } else {
                loadBooks(); // If no genre selected, reload all books
            }
        } catch (Exception e) {
            System.err.println("Error during genre filter: " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Filter failed: " + e.getMessage()));
            books = new ArrayList<>(); // Clear results on error
        }
    }

    /**
     * Validates the input fields for a new book.
     *
     * @return true if all fields are valid, false otherwise.
     */
    private boolean validateBook() {
        boolean valid = true;

        if (newBook.getTitle() == null || newBook.getTitle().trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Title is required"));
            valid = false;
        }

        if (newBook.getAuthor() == null || newBook.getAuthor().trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Author is required"));
            valid = false;
        }

        if (newBook.getPrice() == null || newBook.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Price must be greater than 0"));
            valid = false;
        }

        if (newBook.getStockQuantity() == null || newBook.getStockQuantity() < 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Stock quantity must be 0 or greater"));
            valid = false;
        }

        if (selectedGenreId == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Please select a genre"));
            valid = false;
        }

        return valid;
    }

    /**
     * Sets the selected book for editing in the UI.
     *
     * @param book The book entity to select.
     */
    public void selectBook(Book book) {
        this.selectedBook = book;
        // If the UI allows changing the genre of an existing book,
        // you might also need to set selectedGenreId here based on selectedBook.getGenre().getId()
        if (book != null && book.getGenre() != null) {
            this.selectedGenreId = book.getGenre().getId();
        } else {
            this.selectedGenreId = null;
        }
    }

    /**
     * Clears the search filter and reloads all books.
     */
    public void clearSearch() {
        searchTitle = null;
        selectedGenreId = null;
        loadBooks(); // Reload all books after clearing search/filter
    }

    // Getters and Setters
    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public List<Book> getAvailableBooks() {
        return availableBooks;
    }

    public void setAvailableBooks(List<Book> availableBooks) {
        this.availableBooks = availableBooks;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public Book getSelectedBook() {
        return selectedBook;
    }

    public void setSelectedBook(Book selectedBook) {
        this.selectedBook = selectedBook;
    }

    public Book getNewBook() {
        return newBook;
    }

    public void setNewBook(Book newBook) {
        this.newBook = newBook;
    }

    public Long getSelectedGenreId() {
        return selectedGenreId;
    }

    public void setSelectedGenreId(Long selectedGenreId) {
        this.selectedGenreId = selectedGenreId;
    }

    public String getSearchTitle() {
        return searchTitle;
    }

    public void setSearchTitle(String searchTitle) {
        this.searchTitle = searchTitle;
    }
}
