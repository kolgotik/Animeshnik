# Animeshnik 
Anime recommending telegram bot

This is bot is based on telegram API, SpringBoot, AnilistAPI v2 and as DB it uses MySQL. 

Functionality: 

1. Random - by pressing it user will get random anime. Also if user got anime despite from which button on the bottom of the message there will be 2 buttons 1 - add anime to watchlist 2 - continue rolling - which continues to give user anime from current button. For example if random pressed and continue rolling pressed user will still recieve random anime.
2. By genre - by pressing it user will be offered to choose wanted genres from list, after selection, confirm button will appear, after pressing it user will get anime based on selected genres.
3. By rating - by pressing it user will see 4 options: Average score from 0 - 50, 50 - 60, 60 - 80 and 80 - 100.
4. Added to watchlist animes user can see in watchlist button. If user presses anime from watchlist 3 options will be offered: 1 - Anime Info which sends anime title, genres, start/end dates, avg score, episodes, description and image.

DB operations:

Firstly if new user presses start button, user will be added to db. 
The main purpose of DB is to correctly work with anime details.

