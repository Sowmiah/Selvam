package com.myfirstproject.example.repository;

import com.myfirstproject.example.dao.UserDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MyRepo extends JpaRepository<UserDAO, Long> {

}
