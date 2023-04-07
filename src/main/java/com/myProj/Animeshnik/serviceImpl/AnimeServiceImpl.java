package com.myProj.Animeshnik.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.myProj.Animeshnik.service.AnimeService;
import jakarta.ws.rs.client.ResponseProcessingException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class AnimeServiceImpl implements AnimeService {

    private final OkHttpClient client = new OkHttpClient();

    public int animeId;

    @Value("${api.max-pages}") //~16 000
    int maxPage;

    @Override
    public String extractAnimeTitleTest(String anime) {

        ObjectMapper objectMapper = new ObjectMapper();

        String title;
        try {
            JsonNode rootNode = objectMapper.readTree(anime);
            JsonNode mediaNode = rootNode.path("data").path("Media");

            JsonNode titleNode = mediaNode.path("title");


            title = mediaNode.path("title").path("romaji").asText();


        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            title = "Nani?! Something went wrong... Repeat the operation.";
            log.error("Error occurred on parsing API response: " + e.getMessage());
        }
        return title;
    }

    public String parseTest(String anime) {

        int id;
        String description;
        String title;
        int episodes;
        String result = "";
        String averageScore;
        String genres;

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode rootNode = objectMapper.readTree(anime);
            JsonNode mediaNode = rootNode.path("data").path("Media");
            id = mediaNode.path("id").asInt();
            animeId = id;
            description = mediaNode.path("description").asText()
                    .replaceAll("<br>", "")
                    .replaceAll("<i>", "")
                    .replaceAll("</i>", "")
                    .replaceAll("</br>", "")
                    .replaceAll("<b>", "")
                    .replaceAll("</b>", "")
                    .replaceAll("<a href=\"", "")
                    .replaceAll("\">", "")
                    .replaceAll("&rsquo;", "")
                    .replaceAll("&ldquo;", "")
                    .replaceAll("&rdquo;", "")
                    .replaceAll("&amp;", "")
                    .replaceAll("</a>", "");

            averageScore = String.valueOf(mediaNode.path("averageScore").asInt());
            episodes = mediaNode.path("episodes").asInt();
            genres = mediaNode.path("genres").asText();
            JsonNode titleNode = mediaNode.path("title");

            if (titleNode.hasNonNull("english")) {
                title = mediaNode.path("title").path("english").asText();
            } else {
                title = mediaNode.path("title").path("romaji").asText();
            }
            if (!mediaNode.hasNonNull("description")) {
                description = "Description is not available";
            }
            if (!mediaNode.hasNonNull("genres") || mediaNode.path("genres").isEmpty() || mediaNode.path("genres").isNull()) {
                genres = "Genres are not available";
            } else {
                JsonNode genresNode = mediaNode.path("genres");
                StringBuilder stringBuilder = new StringBuilder();
                for (JsonNode genreNode : genresNode) {
                    stringBuilder.append(genreNode.asText());
                    stringBuilder.append(", ");
                }
                genres = stringBuilder.toString().replaceAll(", $", "");
            }
            if (mediaNode.hasNonNull("averageScore")) {
                averageScore = averageScore + " / 100";
            } else {
                averageScore = "Score is not available";

            }

            result = "Anime title: " + title + "\n"
                    + "\n" + "Genres: " + genres + "\n"
                    + "\n" + "Average Score: " + averageScore + "\n"
                    + "\n" + "Episodes: " + episodes + "\n"
                    + "\n"
                    + "Description: " + description;

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            result = "Nani?! Something went wrong... Repeat the operation.";
            log.error("Error occurred on parsing API response: " + e.getMessage());
        }

        return result;
    }

    public String test() {
        String url = "https://graphql.anilist.co";
        Random random = new Random();
        int randomPage = random.nextInt(maxPage) + 1;
        Map<String, Object> variables = new HashMap<>();
        variables.put("page", randomPage);

        String query = """
                query {
                    Media(id: 130588, type: ANIME) {
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
                """;
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
        String responseBody = null;
        try (Response response = client.newCall(request).execute()) {

            responseBody = response.body().string();

            log.info("API response: " + responseBody);
        } catch (ResponseProcessingException | IOException e) {
            log.error("Error occurred during response processing: " + e.getMessage());
        }
        return responseBody;

    }

    @Override
    public String getRandomAnime() {

        Random random = new Random();
        String url = "https://graphql.anilist.co";
        int randomPage = random.nextInt(maxPage) + 1;

        Map<String, Object> variables = new HashMap<>();
        variables.put("page", randomPage);

        String query = """
                query ($page: Int) {
                  Page(page: $page, perPage: 1) {
                    media(type: ANIME) {
                      id
                      startDate {
                        year
                        month
                      }
                      endDate {
                        year
                        month
                      }
                      title {
                        english
                        romaji
                      }
                      episodes
                      description
                      averageScore
                      genres
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
        String responseBody = null;
        try (Response response = client.newCall(request).execute()) {

            responseBody = response.body().string();

            log.info("API response: " + responseBody);
        } catch (ResponseProcessingException | IOException e) {
            log.error("Error occurred during response processing: " + e.getMessage());
        }
        return responseBody;
    }

    @Override
    public String getAnimeDescription(Integer animeId) {
        String url = "https://graphql.anilist.co";

        Map<String, Object> variables = new HashMap<>();
        variables.put("id", animeId);

        String query = """
                query ($id: Int) {
                   Media(id: $id, type: ANIME) {
                     id
                      startDate {
                        year
                        month
                      }
                      endDate {
                        year
                        month
                      }
                      title {
                        english
                        romaji
                      }
                      episodes
                      description
                      averageScore
                      genres
                   }
                 }
                 """;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query);
        requestBody.put("variables", variables);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonRequestBody = null;
        try {
            jsonRequestBody = objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            log.error("Error occurred during request body processing: " + e.getMessage());
        }

        RequestBody body = RequestBody.create(jsonRequestBody, okhttp3.MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        String responseBody = null;
        try (Response response = client.newCall(request).execute()) {
            responseBody = response.body().string();
            log.info("API response: " + responseBody);
        } catch (ResponseProcessingException | IOException e) {
            log.error("Error occurred during response processing: " + e.getMessage());
        }

        return responseBody;
    }

    @Override
    public String getAnimeTitleFromResponse(String anime) {

        String title;

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode rootNode = objectMapper.readTree(anime);
            JsonNode mediaNode = rootNode.path("data").path("Page").path("media").get(0);
            JsonNode titleNode = mediaNode.path("title");

            title = mediaNode.path("title").path("romaji").asText();

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return title;
    }

    @Override
    public List<String> retrievedAnimeList(String anime) {
        List<String> list = new ArrayList<>();
        list.add(anime);
        return list;
    }

    @Override
    public String extractAnimeTitle(String anime) {

        String regex = "Anime title:\\s*(.*?)\\s*Genres:";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(anime);
        String extractedTitle = "";
        if (matcher.find()) {
            extractedTitle = matcher.group(1);
        }
        return extractedTitle;
    }

    @Override
    public Integer getAnimeIdFromAPI(String anime) {
        String url = "https://graphql.anilist.co";

        Map<String, Object> variables = new HashMap<>();
        variables.put("title", anime);

        String query = """
                query ($title: String) {
                  Media(search: $title, type: ANIME) {
                    id
                  }
                }""";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query);
        requestBody.put("variables", variables);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonRequestBody = null;
        try {
            jsonRequestBody = objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            log.error("Error occurred during request body processing: " + e.getMessage());
        }

        RequestBody body = RequestBody.create(jsonRequestBody, okhttp3.MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        String responseBody = null;
        try (Response response = client.newCall(request).execute()) {
            responseBody = response.body().string();
            log.info("API response: " + responseBody);
        } catch (ResponseProcessingException | IOException e) {
            log.error("Error occurred during response processing: " + e.getMessage());
        } catch (HttpClientErrorException.NotFound notFoundException) {
            log.error("Anime not found" + notFoundException.getMessage());
            return Integer.valueOf(404 + " Not found");
        }

        if (responseBody != null) {
            try {
                JsonNode responseJson = objectMapper.readTree(responseBody);
                if (responseJson.has("data")) {
                    JsonNode mediaNode = responseJson.get("data").get("Media");
                    if (mediaNode != null && !mediaNode.isNull() && mediaNode.has("id")) {
                        return mediaNode.get("id").asInt();
                    }
                }
            } catch (JsonProcessingException e) {
                log.error("Error occurred during response parsing: " + e.getMessage());
            }
        }

        return null;
    }


    @Override
    public String parseJSONAnime(String anime) {

        int id;
        String description;
        String title;
        int episodes;
        String result = "";
        String averageScore;
        String genres;
        String startDate;
        String endDate;

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode rootNode = objectMapper.readTree(anime);
            JsonNode mediaNode = rootNode.path("data").path("Page").path("media").get(0);
            id = mediaNode.path("id").asInt();
            animeId = id;
            description = mediaNode.path("description").asText()
                    .replaceAll("<br>", "")
                    .replaceAll("<i>", "")
                    .replaceAll("</i>", "")
                    .replaceAll("</br>", "")
                    .replaceAll("<b>", "")
                    .replaceAll("</b>", "")
                    .replaceAll("<a href=\"", "")
                    .replaceAll("\">", "")
                    .replaceAll("&rsquo;", "")
                    .replaceAll("&ldquo;", "")
                    .replaceAll("&rdquo;", "")
                    .replaceAll("&amp;", "")
                    .replaceAll("<\\/I>", "")
                    .replaceAll("</a>", "");

            averageScore = String.valueOf(mediaNode.path("averageScore").asInt());
            episodes = mediaNode.path("episodes").asInt();

            JsonNode titleNode = mediaNode.path("title");

            endDate = formatDate(mediaNode.path("endDate"));
            startDate = formatDate(mediaNode.path("startDate"));


            if (titleNode.hasNonNull("english")) {
                title = mediaNode.path("title").path("english").asText();
            } else {
                title = mediaNode.path("title").path("romaji").asText();
            }
            if (!mediaNode.hasNonNull("description")) {
                description = "Description is not available";
            }
            if (!mediaNode.hasNonNull("genres") || mediaNode.path("genres").isEmpty() || mediaNode.path("genres").isNull()) {
                genres = "Genres are not available";
            } else {
                JsonNode genresNode = mediaNode.path("genres");
                StringBuilder stringBuilder = new StringBuilder();
                for (JsonNode genreNode : genresNode) {
                    stringBuilder.append(genreNode.asText());
                    stringBuilder.append(", ");
                }
                genres = stringBuilder.toString().replaceAll(", $", "");
            }
            if (mediaNode.hasNonNull("averageScore")) {
                averageScore = averageScore + " / 100";
            } else {
                averageScore = "Score is not available";

            }

            result = "Anime title: " + title + "\n"
                    + "\n" + "Genres: " + genres + "\n"
                    + "\n" + "Start Date: " + startDate + "\n"
                    + "\n" + "End Date: " + endDate + "\n"
                    + "\n" + "Average Score: " + averageScore + "\n"
                    + "\n" + "Episodes: " + episodes + "\n"
                    + "\n"
                    + "Description: " + description + "\n\n" + "press for random if bored -> /random" + "\n\n"
                    + "\u200B";

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            result = "Nani?! Something went wrong... Repeat the operation.";
            log.error("Error occurred on parsing API response: " + e.getMessage());
        }

        return result;
    }

    public String formatDate(JsonNode dateNode) {
        String month;
        String year;
        if (dateNode.isNull()) {
            return "No data";
        }
        if (dateNode.hasNonNull("month")) {
            month = dateNode.path("month").asText();
            if (Integer.parseInt(month) < 10) {
                month = "0" + month;
            }
        } else {
            month = "unknown";
        }
        if (dateNode.hasNonNull("year")) {
            year = dateNode.path("year").asText();
        } else {
            year = "unknown";
        }

        return month + "/" + year;
    }
}


