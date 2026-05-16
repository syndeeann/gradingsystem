package com.gradingsystem.repository;

import com.gradingsystem.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {

    Optional<Permission> findByResourceAndAction(String resource, String action);

    List<Permission> findByResource(String resource);
}
