package com.kosta.geekku.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kosta.geekku.entity.InteriorRequest;
import com.kosta.geekku.entity.User;

public interface InteriorRequestRepository extends JpaRepository<InteriorRequest, Integer> {
	Page<InteriorRequest> findAllByUser(Optional<User> user, Pageable pageable);
}
