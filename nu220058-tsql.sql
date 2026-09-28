CREATE OR ALTER PROCEDURE SP_REWARD_USER_
    @userId INT,
    @movieId INT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @userRatingCount INT;
    SELECT @userRatingCount = COUNT(*) FROM Rating WHERE IdU = @userId;

    IF @userRatingCount < 10 RETURN;

    DECLARE @globalAvgBefore DECIMAL(10,3);
    SELECT @globalAvgBefore = AVG(CAST(Score AS DECIMAL(10,3))) 
    FROM Rating 
    WHERE IdM = @movieId AND IdU <> @userId;

    IF ISNULL(@globalAvgBefore, 0) < 6  --ako nema ocena uzimamo kao da je prosek manji od 6
    IF EXISTS (
        SELECT 1 
        FROM MovieGenre mg
        WHERE mg.IdM = @movieId
        AND mg.IdG IN (
            SELECT mg2.IdG
            FROM Rating r
            JOIN MovieGenre mg2 ON r.IdM = mg2.IdM
            WHERE r.IdU = @userId
            GROUP BY mg2.IdG
            HAVING AVG(CAST(r.Score AS DECIMAL(10,3))) >= 8
        )
    )
    BEGIN
        UPDATE [User] SET Rewards = ISNULL(Rewards, 0) + 1 WHERE IdU = @userId;
    END
END
GO


CREATE OR ALTER TRIGGER TR_UPDATE_MOVIE_TREND
ON Rating
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON

    UPDATE Movie
    SET AverageRating = (
        SELECT ISNULL(AVG(CAST(Score AS DECIMAL(10,3))), 0)
        FROM Rating
        WHERE Rating.IdM = Movie.IdM
    )
    WHERE IdM IN (SELECT IdM FROM inserted UNION SELECT IdM FROM deleted);

    DECLARE @MaxRecentRatings INT;
    
    SELECT TOP 1 @MaxRecentRatings = COUNT(*)
    FROM Rating
    WHERE DateRated > DATEADD(DAY, -30, GETDATE())
    GROUP BY IdM
    ORDER BY COUNT(*) DESC;

    SET @MaxRecentRatings = ISNULL(@MaxRecentRatings, 0);
  

    UPDATE Movie
    SET Status = CASE 

         WHEN (
            SELECT AVG(CAST(Score AS DECIMAL(10,3))) FROM (
                SELECT TOP 5 Score FROM Rating WHERE IdM = Movie.IdM ORDER BY DateRated DESC
            ) AS Last5
        ) >= (AverageRating + 1.0) THEN 'Rising'

        WHEN (
            SELECT AVG(CAST(Score AS DECIMAL(10,3))) FROM (
                SELECT TOP 5 Score FROM Rating WHERE IdM = Movie.IdM ORDER BY DateRated DESC
            ) AS Last5
        ) <= (AverageRating - 1.0) THEN 'Falling'

        WHEN (SELECT COUNT(*) FROM Rating WHERE IdM = Movie.IdM) >= 3 AND AverageRating >= 8.0 THEN 'Classic'
        
        WHEN (SELECT COUNT(*) FROM Rating WHERE IdM = Movie.IdM AND DateRated > DATEADD(DAY, -30, GETDATE())) = @MaxRecentRatings 
            AND @MaxRecentRatings > 0 THEN 'Trending'

        ELSE NULL
    END
    WHERE IdM IN (SELECT IdM FROM inserted UNION SELECT IdM FROM deleted);


    IF EXISTS (SELECT 1 FROM inserted)
    BEGIN
        DECLARE @insIdU INT, @insIdM INT;
        DECLARE reward_cursor CURSOR FOR SELECT IdU, IdM FROM inserted;
        OPEN reward_cursor;
        FETCH NEXT FROM reward_cursor INTO @insIdU, @insIdM;
        WHILE @@FETCH_STATUS = 0
        BEGIN
            EXEC SP_REWARD_USER_ @insIdU, @insIdM;
            FETCH NEXT FROM reward_cursor INTO @insIdU, @insIdM;
        END
        CLOSE reward_cursor;
        DEALLOCATE reward_cursor;
    END
END
GO


CREATE OR ALTER TRIGGER TR_BLOCK_EXTREME
ON Rating
FOR INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @userId INT, @movieId INT, @score INT;

    DECLARE row_cursor CURSOR FOR 
    SELECT IdU, IdM, Score FROM inserted;

    OPEN row_cursor;
    FETCH NEXT FROM row_cursor INTO @userId, @movieId, @score;

    WHILE @@FETCH_STATUS = 0
    BEGIN
        IF @score = 1 OR @score = 10
        BEGIN
            IF EXISTS (
                SELECT 1 
                FROM MovieGenre mg
                WHERE mg.IdM = @movieId
                AND (
                    (SELECT COUNT(*) FROM Rating r
                     JOIN MovieGenre mg2 ON r.IdM = mg2.IdM
                     WHERE r.IdU = @userId AND mg2.IdG = mg.IdG AND r.Score IN (1, 10) AND r.IdM <> @movieId) > 3
                )
                AND (
                    (SELECT COUNT(*) FROM Rating r
                     JOIN MovieGenre mg2 ON r.IdM = mg2.IdM
                     WHERE r.IdU = @userId AND mg2.IdG = mg.IdG AND r.Score IN (6, 7, 8) AND r.IdM <> @movieId) < 3
                )
            )
            BEGIN
                CLOSE row_cursor;
                DEALLOCATE row_cursor;
                RAISERROR('Korisnik je blokiran za ekstremne ocene (1 ili 10) u ovom žanru.', 16, 1);
                ROLLBACK TRANSACTION;
                RETURN;
            END
        END
        FETCH NEXT FROM row_cursor INTO @userId, @movieId, @score;
    END
    CLOSE row_cursor;
    DEALLOCATE row_cursor;
END
GO