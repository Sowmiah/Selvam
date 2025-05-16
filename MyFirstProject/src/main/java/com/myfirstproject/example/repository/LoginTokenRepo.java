package com.myfirstproject.example.repository;

import com.myfirstproject.example.dao.DailyLoginTokenDAO;
import com.myfirstproject.example.dao.UserDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginTokenRepo extends JpaRepository<DailyLoginTokenDAO, Long>  {


}
