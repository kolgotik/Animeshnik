package com.myProj.Animeshnik.service;

import okhttp3.Response;

public interface AnimeService {

    void testGetAnime();
    String  testGetAnimeString();

    String parseJSONAnime(String response);

    String getRandomAnime();

}
