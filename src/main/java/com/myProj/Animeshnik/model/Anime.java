package com.myProj.Animeshnik.model;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Slf4j
@Component
public class Anime {

    @Id
    private Integer id;

    private String title;

    private String description;

    private int episodes;

    private int averageScore;

    private String overallInfo;


}
