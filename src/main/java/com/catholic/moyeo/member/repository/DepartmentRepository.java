package com.catholic.moyeo.member.repository;

import com.catholic.moyeo.member.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}