package session;

import entities.Book;
import entities.Genre; // Although not directly used in methods, it might be for constructors/data transfer
import java.math.BigDecimal;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.NoResultException; // Added for consistency with findById/other lookups

@Stateless
public class BookService {

    @PersistenceContext(unitName = "BookstoreApp-ejbPU")
    private EntityManager em;

    /**
     * Creates a new book in the database.
     * This method assumes Container-Managed Transactions (CMT).
     *
     * @param book The Book entity to be persisted.
     * @return The persisted Book entity.
     * @throws RuntimeException if an error occurs during book creation.
     */
    public Book createBook(Book book) {
        try {
            em.persist(book);
            // With CMT, em.flush() is often not explicitly needed here,
            // as the transaction will commit (and flush) on method exit.
            return book;
        } catch (Exception e) {
            System.err.println("Error creating book: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error creating book: " + e.getMessage(), e);
        }
    }

    /**
     * Finds a book by its ID.
     *
     * @param id The ID of the book.
     * @return The Book entity if found, null otherwise.
     */
    public Book findById(Long id) {
        try {
            return em.find(Book.class, id);
        } catch (Exception e) {
            System.err.println("Error finding book by ID: " + e.getMessage());
            e.printStackTrace();
            return null; // Return null on error, similar to NoResultException for consistency
        }
    }

    /**
     * Retrieves all books from the database.
     *
     * @return A list of all Book entities.
     */
    public List<Book> getAllBooks() {
        try {
            TypedQuery<Book> query = em.createNamedQuery("Book.findAll", Book.class);
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error retrieving all books: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error retrieving all books: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves books belonging to a specific genre.
     *
     * @param genreId The ID of the genre.
     * @return A list of Book entities belonging to the specified genre.
     */
    public List<Book> getBooksByGenre(Long genreId) {
        try {
            TypedQuery<Book> query = em.createNamedQuery("Book.findByGenre", Book.class);
            query.setParameter("genreId", genreId);
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error retrieving books by genre: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error retrieving books by genre: " + e.getMessage(), e);
        }
    }

    /**
     * Searches for books by title (case-insensitive, partial match).
     *
     * @param title The title (or part of it) to search for.
     * @return A list of Book entities matching the title.
     */
    public List<Book> searchBooksByTitle(String title) {
        try {
            TypedQuery<Book> query = em.createNamedQuery("Book.findByTitle", Book.class);
            query.setParameter("title", "%" + title.toLowerCase() + "%"); // Assuming title in DB is also lowercased or query handles it
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error searching books by title: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error searching books by title: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing book in the database.
     *
     * @param book The detached Book entity with updated information.
     * @return The merged (managed) Book entity.
     * @throws RuntimeException if an error occurs during book update.
     */
    public Book updateBook(Book book) {
        try {
            return em.merge(book);
        } catch (Exception e) {
            System.err.println("Error updating book: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error updating book: " + e.getMessage(), e);
        }
    }

    /**
     * Updates the price of a specific book.
     *
     * @param bookId The ID of the book to update.
     * @param newPrice The new price for the book.
     * @return The updated Book entity if found, null otherwise.
     * @throws RuntimeException if an error occurs during price update.
     */
    public Book updateBookPrice(Long bookId, BigDecimal newPrice) {
        try {
            Book book = findById(bookId);
            if (book != null) {
                book.setPrice(newPrice);
                return em.merge(book);
            }
            return null; // Book not found
        } catch (Exception e) {
            System.err.println("Error updating book price: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error updating book price: " + e.getMessage(), e);
        }
    }

    /**
     * Updates the stock quantity of a specific book.
     *
     * @param bookId The ID of the book to update.
     * @param newStock The new stock quantity for the book.
     * @return The updated Book entity if found, null otherwise.
     * @throws RuntimeException if an error occurs during stock update.
     */
    public Book updateBookStock(Long bookId, Integer newStock) {
        try {
            Book book = findById(bookId);
            if (book != null) {
                book.setStockQuantity(newStock);
                return em.merge(book);
            }
            return null; // Book not found
        } catch (Exception e) {
            System.err.println("Error updating book stock: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error updating book stock: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a book from the database by its ID.
     *
     * @param bookId The ID of the book to delete.
     * @throws RuntimeException if an error occurs during book deletion.
     */
    public void deleteBook(Long bookId) {
        try {
            Book book = findById(bookId); // Use findById for consistency
            if (book != null) {
                em.remove(book);
            }
        } catch (Exception e) {
            System.err.println("Error deleting book: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error deleting book: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if a book is available for a given quantity.
     *
     * @param bookId The ID of the book.
     * @param quantity The desired quantity.
     * @return true if the book is available in the requested quantity, false otherwise.
     */
    public boolean isBookAvailable(Long bookId, int quantity) {
        try {
            Book book = findById(bookId);
            return book != null && book.getStockQuantity() != null && book.getStockQuantity() >= quantity;
        } catch (Exception e) {
            System.err.println("Error checking book availability: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Decreases the stock quantity of a book after a purchase.
     *
     * @param bookId The ID of the book.
     * @param quantity The quantity to decrease by.
     * @return true if stock was successfully decreased, false otherwise (e.g., insufficient stock).
     * @throws RuntimeException if an error occurs during stock decrease.
     */
    public boolean decreaseStock(Long bookId, int quantity) {
        try {
            Book book = findById(bookId);
            if (book != null && book.getStockQuantity() != null && book.getStockQuantity() >= quantity) {
                book.decreaseStock(quantity); // Assuming Book entity has this method
                em.merge(book);
                return true;
            }
            return false; // Book not found or insufficient stock
        } catch (Exception e) {
            System.err.println("Error decreasing stock: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error decreasing stock: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all books that have a stock quantity greater than 0.
     *
     * @return A list of available Book entities, ordered by title.
     */
    public List<Book> getAvailableBooks() {
        try {
            TypedQuery<Book> query = em.createQuery(
                "SELECT b FROM Book b WHERE b.stockQuantity > 0 ORDER BY b.title", Book.class);
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error retrieving available books: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error retrieving available books: " + e.getMessage(), e);
        }
    }
}
