package session;

import entities.Genre;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

@Stateless
public class GenreService {

    @PersistenceContext(unitName = "BookstoreApp-ejbPU")
    private EntityManager em;

    /**
     * Creates a new genre in the database.
     * This method assumes Container-Managed Transactions (CMT).
     *
     * @param genre The Genre entity to be persisted.
     * @return The persisted Genre entity.
     * @throws RuntimeException if an error occurs during genre creation.
     */
    public Genre createGenre(Genre genre) {
        try {
            em.persist(genre);
            // With CMT, em.flush() is often not explicitly needed here.
            return genre;
        } catch (Exception e) {
            System.err.println("Error creating genre: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error creating genre: " + e.getMessage(), e);
        }
    }

    /**
     * Finds a genre by its ID.
     *
     * @param id The ID of the genre.
     * @return The Genre entity if found, null otherwise.
     */
    public Genre findById(Long id) {
        try {
            return em.find(Genre.class, id);
        } catch (Exception e) {
            System.err.println("Error finding genre by ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Finds a genre by its name.
     *
     * @param genreName The name of the genre to search for.
     * @return The Genre entity if found, null otherwise.
     */
    public Genre findByName(String genreName) {
        try {
            TypedQuery<Genre> query = em.createNamedQuery("Genre.findByName", Genre.class);
            query.setParameter("genreName", genreName);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null; // Genre not found
        } catch (Exception e) {
            System.err.println("Error finding genre by name: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves all genres from the database.
     *
     * @return A list of all Genre entities.
     */
    public List<Genre> getAllGenres() {
        try {
            TypedQuery<Genre> query = em.createNamedQuery("Genre.findAll", Genre.class);
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error retrieving all genres: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error retrieving all genres: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing genre in the database.
     *
     * @param genre The detached Genre entity with updated information.
     * @return The merged (managed) Genre entity.
     * @throws RuntimeException if an error occurs during genre update.
     */
    public Genre updateGenre(Genre genre) {
        try {
            return em.merge(genre);
        } catch (Exception e) {
            System.err.println("Error updating genre: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error updating genre: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a genre from the database by its ID.
     *
     * @param genreId The ID of the genre to delete.
     * @throws RuntimeException if an error occurs during genre deletion.
     */
    public void deleteGenre(Long genreId) {
        try {
            Genre genre = findById(genreId); // Use findById for consistency
            if (genre != null) {
                em.remove(genre);
            }
        } catch (Exception e) {
            System.err.println("Error deleting genre: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error deleting genre: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if a genre name already exists in the database.
     *
     * @param genreName The genre name to check.
     * @return true if the genre name exists, false otherwise.
     */
    public boolean genreExists(String genreName) {
        try {
            return findByName(genreName) != null;
        } catch (Exception e) {
            System.err.println("Error checking if genre exists: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
