package com.gradingsystem.repository;

import com.gradingsystem.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    /** Fetches all roles with their permission sets in a single query. */
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions ORDER BY r.id")
    List<Role> findAllWithPermissions();

    /** Fetches a single role with its permission set. */
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.id = :id")
    Optional<Role> findByIdWithPermissions(@Param("id") Integer id);
}
