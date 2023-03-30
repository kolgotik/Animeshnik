package com.myProj.Animeshnik.service;

import java.util.List;

public interface AnimeService {

    //void testGetAnime();
    //String  testGetAnimeString();

    String parseJSONAnime(String anime);

    String getRandomAnime();

    String getAnimeDescription(String animeName);

    String getAnimeTitleFromResponse(String anime);

    List<String> retrievedAnimeList(String anime);

    String extractAnimeTitle(String anime);

}
