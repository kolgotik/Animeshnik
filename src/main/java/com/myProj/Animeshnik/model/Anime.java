package com.myProj.Animeshnik.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Slf4j
@Entity
public class Anime {

    @Id
    private long id;

    private String title;

    private String description;

    private int episodes;

    private int averageScore;


}
