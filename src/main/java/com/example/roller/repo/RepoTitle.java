package com.example.roller.repo;


import com.example.roller.entity.TitleList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoTitle extends JpaRepository<TitleList, Long> {
}
