package com.myProj.Animeshnik.service;

import java.util.List;

public interface AnimeService {

    String parseJSONAnime(String anime);

    String extractImgLink(String response);

    Boolean checkForNextPage(String response);

    String getAnimeByID(int id);

    String getRandomAnime();

    String getAnimeDescription(Integer animeId);

    String getAnimeTitleFromResponse(String anime);

    List<String> retrievedAnimeList(String anime);

    String extractAnimeTitleTest(String anime);

    String extractAnimeTitle(String anime);

    Integer getAnimeIdFromJSON(String unparsedAnime);

    Integer getAmountOfAvailablePages(List<String> selectedGenres);
    public String test();
    String parseTest(String anime);

    String parseJSONAnime(String anime, String imgLink);
}
