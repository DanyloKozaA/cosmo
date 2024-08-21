package com.example.roller.repo;

import com.example.roller.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoTransac extends JpaRepository<Transaction, Long> {
}