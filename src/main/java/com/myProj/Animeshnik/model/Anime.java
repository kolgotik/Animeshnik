package com.myProj.Animeshnik.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Slf4j
@Component
public class Anime {

    private long id;

    private String title;

    private String description;

    private int episodes;

    private int averageScore;

    private List<User> users;

}
