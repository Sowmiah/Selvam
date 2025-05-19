package com.myfirstproject.example.service;

import com.myfirstproject.example.dao.UserDAO;
import com.myfirstproject.example.dto.UserDTO;
import com.myfirstproject.example.repository.MyRepo;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


@Service
public class MyService {

    private MyRepo repo;

    @Autowired
    public MyService(MyRepo repo) {
        this.repo = repo;
    }

    public String setAllItems() {
        UserDTO dto = new UserDTO();
        List<UserDTO> list = new ArrayList<>();

        dto.setFirstName("Hello");
        dto.setLastName("World");
        dto.setRole("Engineer");

        list.add(dto);

        UserDAO dao = new UserDAO();

        dao.setFirstName(dto.getFirstName());
        dao.setLastName(dto.getLastName());
        dao.setRole(dto.getRole());

        this.repo.save(dao);

        return "Successfully Inserted";

    }

    public List<UserDTO> getAllItems() {

        List<UserDAO> dao = this.repo.findAll();

        List<UserDTO> list = new ArrayList<>();

        for(UserDAO user: dao) {
            UserDTO dto = new UserDTO();
            dto.setId(user.getId());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setRole(user.getRole());
            list.add(dto);
        }

        return list;
    }

}
