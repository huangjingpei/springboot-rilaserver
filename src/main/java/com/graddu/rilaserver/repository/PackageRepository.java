package com.graddu.rilaserver.repository;

import com.graddu.rilaserver.entity.Package;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageRepository extends JpaRepository<Package, Long> {
} 