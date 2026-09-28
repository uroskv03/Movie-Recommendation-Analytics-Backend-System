# Movie-Recommendation-Analytics-Backend-System

Backend system for movie management, user rating analysis and personalized recommendations.

## Tech Stack
* Language: Java
* Database Connection: JDBC
* Database Tools: SQL Server Management Studio 22


* **Data Access Layer (Java / JDBC):** Implemented interface methods using JDBC with explicit transaction handling (`commit`/`rollback`) to maintain data integrity.
* **Automated Trend Tracking (T-SQL Triggers):** Database triggers (`TR_UPDATE_MOVIE_TREND`) automatically track and update movie status (*Trending, Rising, Falling, Classic*) on every rating change.
* **Business Rule Validation (T-SQL Triggers):** Trigger mechanism (`TR_BLOCK_EXTREME`) to prevent extreme rating abuse (1 or 10) based on user activity history within specific genres.
* **User Rewards System (Stored Procedures):** Stored procedure (`SP_REWARD_USER_`) for identifying and rewarding users rating underrated movies within their favorite genres.
* **Recommendation & Profiling Engine:** Logic for calculating favorite genres, hidden gems, categorizing user profiles (*Curious, Focused, Undefined*).
