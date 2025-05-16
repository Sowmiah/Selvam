package com.myfirstproject.example.dao;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Table(name = "LoginToken")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DailyLoginTokenDAO {

    @Id
    @Column(name="id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="login_token")
    private String loginToken;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLoginToken() {
        return loginToken;
    }

    public void setLoginToken(String loginToken) {
        this.loginToken = loginToken;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DailyLoginTokenDAO that = (DailyLoginTokenDAO) o;
        return Objects.equals(id, that.id) && Objects.equals(loginToken, that.loginToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, loginToken);
    }

    @Override
    public String toString() {
        return "DailyLoginTokenDAO{" +
                "id=" + id +
                ", loginToken='" + loginToken + '\'' +
                '}';
    }
}
