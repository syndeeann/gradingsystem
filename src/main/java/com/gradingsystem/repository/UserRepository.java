package com.gradingsystem.repository;

import com.gradingsystem.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // Exclude the target user when checking uniqueness during update
    boolean existsByUsernameAndIdNot(String username, String id);

    boolean existsByEmailAndIdNot(String email, String id);

    List<User> findByRoleNameAndActiveTrue(String roleName);

    Page<User> findByActiveTrue(Pageable pageable);

    @Query("SELECT u FROM User u JOIN FETCH u.role r LEFT JOIN FETCH r.permissions WHERE u.username = :username")
    Optional<User> findByUsernameWithPermissions(@Param("username") String username);

    /**
     * Filtered list for the GET /api/users endpoint.
     * All filter params are optional — pass null to skip a filter.
     * A separate countQuery is required because the main query joins role;
     * without it Spring Data would include the JOIN in the COUNT and produce wrong totals.
     */
    @Query(
        value = """
            SELECT u FROM User u
            WHERE (:roleName IS NULL OR u.role.name = :roleName)
              AND (:active   IS NULL OR u.active    = :active)
              AND (
                    :search IS NULL
                    OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))
                  )
            """,
        countQuery = """
            SELECT COUNT(u) FROM User u
            WHERE (:roleName IS NULL OR u.role.name = :roleName)
              AND (:active   IS NULL OR u.active    = :active)
              AND (
                    :search IS NULL
                    OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))
                  )
            """
    )
    Page<User> findWithFilters(
        @Param("roleName") String roleName,
        @Param("active")   Boolean active,
        @Param("search")   String search,
        Pageable pageable
    );
}
