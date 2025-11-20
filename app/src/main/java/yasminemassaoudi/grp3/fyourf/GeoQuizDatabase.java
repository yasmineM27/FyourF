package yasminemassaoudi.grp3.fyourf;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * Base de données locale pour le cache des questions de GeoQuiz
 * Permet le fonctionnement hors ligne
 */
public class GeoQuizDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "geoquiz.db";
    private static final int DATABASE_VERSION = 1;

    // Tables
    private static final String TABLE_QUESTIONS = "questions";
    private static final String TABLE_BADGES = "badges";
    private static final String TABLE_SCORES = "scores";

    // Colonnes - Questions
    private static final String COL_ID = "id";
    private static final String COL_LATITUDE = "latitude";
    private static final String COL_LONGITUDE = "longitude";
    private static final String COL_LOCATION_NAME = "location_name";
    private static final String COL_CORRECT_ANSWER = "correct_answer";
    private static final String COL_OPTIONS = "options";
    private static final String COL_REGION = "region";
    private static final String COL_CATEGORY = "category";
    private static final String COL_DIFFICULTY = "difficulty";
    private static final String COL_TIMESTAMP = "timestamp";
    private static final String COL_ANSWERED = "answered";
    private static final String COL_CORRECT = "correct";

    // Colonnes - Badges
    private static final String COL_BADGE_ID = "badge_id";
    private static final String COL_BADGE_NAME = "badge_name";
    private static final String COL_BADGE_DESCRIPTION = "badge_description";
    private static final String COL_UNLOCKED = "unlocked";
    private static final String COL_UNLOCKED_DATE = "unlocked_date";
    private static final String COL_PROGRESS = "progress";

    // Colonnes - Scores
    private static final String COL_SCORE_ID = "score_id";
    private static final String COL_TOTAL_POINTS = "total_points";
    private static final String COL_CORRECT_ANSWERS = "correct_answers";
    private static final String COL_TOTAL_QUESTIONS = "total_questions";
    private static final String COL_SCORE_DATE = "score_date";

    public GeoQuizDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Créer la table des questions
        String createQuestionsTable = "CREATE TABLE " + TABLE_QUESTIONS + " (" +
                COL_ID + " INTEGER PRIMARY KEY, " +
                COL_LATITUDE + " REAL, " +
                COL_LONGITUDE + " REAL, " +
                COL_LOCATION_NAME + " TEXT, " +
                COL_CORRECT_ANSWER + " TEXT, " +
                COL_OPTIONS + " TEXT, " +
                COL_REGION + " TEXT, " +
                COL_CATEGORY + " TEXT, " +
                COL_DIFFICULTY + " INTEGER, " +
                COL_TIMESTAMP + " LONG, " +
                COL_ANSWERED + " INTEGER, " +
                COL_CORRECT + " INTEGER)";
        db.execSQL(createQuestionsTable);

        // Créer la table des badges
        String createBadgesTable = "CREATE TABLE " + TABLE_BADGES + " (" +
                COL_BADGE_ID + " INTEGER PRIMARY KEY, " +
                COL_BADGE_NAME + " TEXT, " +
                COL_BADGE_DESCRIPTION + " TEXT, " +
                COL_REGION + " TEXT, " +
                COL_UNLOCKED + " INTEGER, " +
                COL_UNLOCKED_DATE + " LONG, " +
                COL_PROGRESS + " INTEGER)";
        db.execSQL(createBadgesTable);

        // Créer la table des scores
        String createScoresTable = "CREATE TABLE " + TABLE_SCORES + " (" +
                COL_SCORE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TOTAL_POINTS + " INTEGER, " +
                COL_CORRECT_ANSWERS + " INTEGER, " +
                COL_TOTAL_QUESTIONS + " INTEGER, " +
                COL_SCORE_DATE + " LONG)";
        db.execSQL(createScoresTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUESTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BADGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES);
        onCreate(db);
    }

    // ===== QUESTIONS =====

    public long addQuestion(GeoQuizQuestion question) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_LATITUDE, question.getLatitude());
        values.put(COL_LONGITUDE, question.getLongitude());
        values.put(COL_LOCATION_NAME, question.getLocationName());
        values.put(COL_CORRECT_ANSWER, question.getCorrectAnswer());
        values.put(COL_OPTIONS, String.join(",", question.getOptions()));
        values.put(COL_REGION, question.getRegion());
        values.put(COL_CATEGORY, question.getCategory());
        values.put(COL_DIFFICULTY, question.getDifficulty());
        values.put(COL_TIMESTAMP, question.getTimestamp());
        values.put(COL_ANSWERED, question.isAnswered() ? 1 : 0);
        values.put(COL_CORRECT, question.isCorrect() ? 1 : 0);
        return db.insert(TABLE_QUESTIONS, null, values);
    }

    public List<GeoQuizQuestion> getAllQuestions() {
        List<GeoQuizQuestion> questions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_QUESTIONS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                GeoQuizQuestion question = new GeoQuizQuestion();
                question.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
                question.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LATITUDE)));
                question.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LONGITUDE)));
                question.setLocationName(cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION_NAME)));
                question.setCorrectAnswer(cursor.getString(cursor.getColumnIndexOrThrow(COL_CORRECT_ANSWER)));
                
                String optionsStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_OPTIONS));
                if (optionsStr != null) {
                    for (String option : optionsStr.split(",")) {
                        question.addOption(option);
                    }
                }
                
                question.setRegion(cursor.getString(cursor.getColumnIndexOrThrow(COL_REGION)));
                question.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)));
                question.setDifficulty(cursor.getInt(cursor.getColumnIndexOrThrow(COL_DIFFICULTY)));
                question.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP)));
                question.setAnswered(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ANSWERED)) == 1);
                question.setCorrect(cursor.getInt(cursor.getColumnIndexOrThrow(COL_CORRECT)) == 1);
                
                questions.add(question);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return questions;
    }

    public List<GeoQuizQuestion> getQuestionsByRegion(String region) {
        List<GeoQuizQuestion> questions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_QUESTIONS, null, COL_REGION + "=?", 
                new String[]{region}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                GeoQuizQuestion question = new GeoQuizQuestion();
                question.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
                question.setLocationName(cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION_NAME)));
                question.setCorrectAnswer(cursor.getString(cursor.getColumnIndexOrThrow(COL_CORRECT_ANSWER)));
                question.setRegion(region);
                questions.add(question);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return questions;
    }

    public void updateQuestion(GeoQuizQuestion question) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ANSWERED, question.isAnswered() ? 1 : 0);
        values.put(COL_CORRECT, question.isCorrect() ? 1 : 0);
        db.update(TABLE_QUESTIONS, values, COL_ID + "=?", 
                new String[]{String.valueOf(question.getId())});
    }

    public void deleteAllQuestions() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_QUESTIONS, null, null);
    }

    // ===== BADGES =====

    public long addBadge(Badge badge) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_BADGE_ID, badge.getId());
        values.put(COL_BADGE_NAME, badge.getName());
        values.put(COL_BADGE_DESCRIPTION, badge.getDescription());
        values.put(COL_REGION, badge.getRegion());
        values.put(COL_UNLOCKED, badge.isUnlocked() ? 1 : 0);
        values.put(COL_UNLOCKED_DATE, badge.getUnlockedDate());
        values.put(COL_PROGRESS, badge.getProgress());
        return db.insert(TABLE_BADGES, null, values);
    }

    public List<Badge> getAllBadges() {
        List<Badge> badges = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BADGES, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Badge badge = new Badge();
                badge.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_BADGE_ID)));
                badge.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_BADGE_NAME)));
                badge.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_BADGE_DESCRIPTION)));
                badge.setRegion(cursor.getString(cursor.getColumnIndexOrThrow(COL_REGION)));
                badge.setUnlocked(cursor.getInt(cursor.getColumnIndexOrThrow(COL_UNLOCKED)) == 1);
                badge.setUnlockedDate(cursor.getLong(cursor.getColumnIndexOrThrow(COL_UNLOCKED_DATE)));
                badge.setProgress(cursor.getInt(cursor.getColumnIndexOrThrow(COL_PROGRESS)));
                badges.add(badge);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return badges;
    }

    public void updateBadge(Badge badge) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_UNLOCKED, badge.isUnlocked() ? 1 : 0);
        values.put(COL_PROGRESS, badge.getProgress());
        values.put(COL_UNLOCKED_DATE, badge.getUnlockedDate());
        db.update(TABLE_BADGES, values, COL_BADGE_ID + "=?", 
                new String[]{String.valueOf(badge.getId())});
    }

    // ===== SCORES =====

    public long addScore(int totalPoints, int correctAnswers, int totalQuestions) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TOTAL_POINTS, totalPoints);
        values.put(COL_CORRECT_ANSWERS, correctAnswers);
        values.put(COL_TOTAL_QUESTIONS, totalQuestions);
        values.put(COL_SCORE_DATE, System.currentTimeMillis());
        return db.insert(TABLE_SCORES, null, values);
    }

    public List<Integer> getTopScores(int limit) {
        List<Integer> scores = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SCORES, new String[]{COL_TOTAL_POINTS}, 
                null, null, null, null, COL_TOTAL_POINTS + " DESC", String.valueOf(limit));

        if (cursor.moveToFirst()) {
            do {
                scores.add(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TOTAL_POINTS)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return scores;
    }
}

