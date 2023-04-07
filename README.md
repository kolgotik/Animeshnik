# Animeshnik someday README will be updated
Anime recommending telegram bot

Currently this bot can register user at db when they first time pressed start button. By now it has /random command which retrieves from AnilistAPI V2 a random anime. 
See info about API here: https://anilist.gitbook.io/anilist-apiv2-docs/. It replies with messege in which user can see anime title, description (if present), 
average score (if present) and episodes. Under message there is a "Add anime to watchlist" button which stores anime title in db according to user. If the "Add anime to watchlist" button is pressed the message is edited to smth like: "Added anime: " + animeTitle + ... + " press /watchlist to see added animes" and it shows added anime. 
