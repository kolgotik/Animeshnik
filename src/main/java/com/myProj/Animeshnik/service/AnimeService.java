package com.myProj.Animeshnik.service;

import com.myProj.Animeshnik.model.Anime;
import okhttp3.Response;

public interface AnimeService {

    //void testGetAnime();
    //String  testGetAnimeString();

    String parseJSONAnime(String anime);

    String getRandomAnime();

    String getAnimeTitleFromResponse(String anime);

}
