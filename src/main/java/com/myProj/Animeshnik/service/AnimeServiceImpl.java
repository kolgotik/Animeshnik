package com.myProj.Animeshnik.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
@Slf4j
public class AnimeServiceImpl implements AnimeService {

    @Autowired
    private AniListAPIv2TotalAvailablePages totalAvailablePages;

    @Override
    public void testGetAnime() {
        OkHttpClient client = new OkHttpClient();

        String url = "https://graphql.anilist.co";

        Map<String, Object> variables = new HashMap<>();
        variables.put("id", 1);

        String query = "query ($id: Int) {\n" +
                "  Media(id: $id, type: ANIME) {\n" +
                "    id\n" +
                "    title {\n" +
                "      romaji\n" +
                "      english\n" +
                "    }\n" +
                "    episodes\n" +
                "  }\n" +
                "}";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query);
        requestBody.put("variables", variables);

        Gson gson = new Gson();
        String jsonRequestBody = gson.toJson(requestBody);

        RequestBody body = RequestBody.create(jsonRequestBody, okhttp3.MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            System.out.println("API response: " + responseBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String testGetAnimeString() {
        OkHttpClient client = new OkHttpClient();

        String url = "https://graphql.anilist.co";

        Map<String, Object> variables = new HashMap<>();
        variables.put("id", 1);

        String query = "query ($id: Int) {\n" +
                "  Media(id: $id, type: ANIME) {\n" +
                "    id\n" +
                "    title {\n" +
                "      romaji\n" +
                "      english\n" +
                "    }\n" +
                "    episodes\n" +
                "  }\n" +
                "}";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query);
        requestBody.put("variables", variables);

        Gson gson = new Gson();
        String jsonRequestBody = gson.toJson(requestBody);

        RequestBody body = RequestBody.create(jsonRequestBody, okhttp3.MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        String res = null;
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            res = parseJSONAnime(responseBody);
            System.out.println("API response: " + responseBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public String getRandomAnime() {
        OkHttpClient client = new OkHttpClient();
        Random random = new Random();
        String url = "https://graphql.anilist.co";

        int maxPage = 16000;
        //int maxPage = 20000;
        int randomPage = random.nextInt(maxPage) + 1;

        Map<String, Object> variables = new HashMap<>();
        variables.put("page", randomPage);

        String query = """
                query ($page: Int) {
                  Page(page: $page, perPage: 1) {
                    media(type: ANIME) {
                      id
                      title {
                        english
                        romaji
                      }
                      episodes
                      description
                      averageScore
                    }
                  }
                }""";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query);
        requestBody.put("variables", variables);

        Gson gson = new Gson();
        String jsonRequestBody = gson.toJson(requestBody);

        RequestBody body = RequestBody.create(jsonRequestBody, okhttp3.MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        String res = null;
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();

            res = parseJSONAnime(responseBody);
            System.out.println("API response: " + responseBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public String parseJSONAnime(String response) {
        ObjectMapper objectMapper = new ObjectMapper();
        int id;
        String englishTitle;
        String description;
        String romajiTitle;
        String title;
        int episodes;
        String result = "";
        String averageScore;
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode mediaNode = rootNode.path("data").path("Page").path("media").get(0);
            id = mediaNode.path("id").asInt();
            description = mediaNode.path("description").asText().replaceAll("<br>", "").replaceAll("<i>", "")
                    .replaceAll("</i>", "")
                    .replaceAll("</br>", "")
                    .replaceAll("<b>", "")
                    .replaceAll("</b>", "")
                    .replaceAll("<a href=\"", "")
                    .replaceAll("\">", "")
                    .replaceAll("</a>", "");

            averageScore = String.valueOf(mediaNode.path("averageScore").asInt());
            episodes = mediaNode.path("episodes").asInt();

            JsonNode titleNode = mediaNode.path("title");

            if (titleNode.hasNonNull("english")) {
                title = mediaNode.path("title").path("english").asText();
            } else {
                title = mediaNode.path("title").path("romaji").asText();
            }
            if (!mediaNode.hasNonNull("description")) {
                description = "Description is not available";
            }
            if (mediaNode.hasNonNull("averageScore")) {
                averageScore = averageScore + " / 100";
            } else {
                averageScore = "Score is not available";

            }

            result = "Anime title: " + title + "\n"
                    + "\n" + "Average Score: " + averageScore + "\n"
                    + "\n"
                    + "Description: " + description + "\n"
                    + "\n"
                    + "Episodes: " + episodes;

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            result = "Nani?! Something went wrong... Repeat the operation.";
            log.error("Error occurred on parsing API response: " + e.getMessage());
        }

        return result;
    }

}


