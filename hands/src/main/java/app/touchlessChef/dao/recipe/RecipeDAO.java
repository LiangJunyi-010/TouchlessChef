package app.touchlessChef.dao.recipe;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import app.touchlessChef.model.Ingredient;
import app.touchlessChef.model.Instruction;
import app.touchlessChef.model.Recipe;

public class RecipeDAO {
    private final SQLiteDatabase db;

    private final IngredientDAO ingredientDAO;
    private final InstructionDAO instructionDAO;

    public RecipeDAO(SQLiteDatabase db) {
        this.db = db;

        ingredientDAO = new IngredientDAO(db);
        instructionDAO = new InstructionDAO(db);
    }

    public void insert(Recipe recipe) {
        if (recipe.getIngredients() == null || recipe.getInstructions() == null)
            throw new IllegalStateException("Cannot insert recipe: the recipe is incomplete.");

        long newRecipeId = insert(recipe.getName(), recipe.getCategory(), recipe.getDescription(),
                recipe.getImagePath(), recipe.getTime(), recipe.getMealType());
        Log.i("DAO", "Inserted new recipe : " + newRecipeId);
        Log.i("DAO", "New recipe has " + recipe.getInstructions().size() + " instructions.");
        for (Ingredient ingredient : recipe.getIngredients()) {
            ingredient.setRecipeId(newRecipeId);
            ingredientDAO.insert(ingredient);
            Log.i("DAO", "Inserted " + ingredient);
        }
        for (Instruction instruction : recipe.getInstructions()) {
            instruction.setRecipeId(newRecipeId);
            instructionDAO.insert(instruction);
            Log.i("DAO", "Inserted " + instruction);
        }
    }

    private long insert(String name, String category, String description, String imagePath,
                        String time, String mealType) {
        ContentValues values = new ContentValues();
        values.put(Config.KEY_NAME, name);
        values.put(Config.KEY_CATEGORY, category);
        values.put(Config.KEY_DESCRIPTION, description);
        values.put(Config.KEY_IMAGE_PATH, imagePath);
        values.put(Config.KEY_TIME, time);
        values.put(Config.KEY_MEAL_TYPE, mealType);
        return db.insert(Config.TABLE_NAME, null, values);
    }

    public List<Recipe> selectAllByCategory(String category) {
        List<Recipe> recipes = new ArrayList<>();
        try (Cursor cursor = db.query(Config.TABLE_NAME,
                new String[]{Config.KEY_ID, Config.KEY_NAME, Config.KEY_CATEGORY,
                        Config.KEY_DESCRIPTION, Config.KEY_IMAGE_PATH,
                        Config.KEY_TIME, Config.KEY_MEAL_TYPE},
                Config.KEY_CATEGORY + " = ?", new String[]{category},
                null, null, null)) {
            if (cursor.moveToFirst()) {
                do {
                    Recipe recipe = new Recipe(
                            cursor.getLong(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getString(5),
                            cursor.getString(6));
                    recipe.setIngredients(ingredientDAO.selectAllByRecipeId(recipe.getId()));
                    recipe.setInstructions(instructionDAO.selectAllByRecipeId(recipe.getId()));
                    recipes.add(recipe);

                } while (cursor.moveToNext());
            }
        }

        Log.i("DAO", "RecipeDAO returning: " + recipes);
        return recipes;
    }

    public void update(Recipe recipe) {
        ingredientDAO.deleteAllByRecipeId(recipe.getId());
        instructionDAO.deleteAllByRecipeId(recipe.getId());
        for (Ingredient ingredient : recipe.getIngredients()) {
            ingredient.setRecipeId(recipe.getId());
            ingredientDAO.insert(ingredient);
        }
        for (Instruction instruction : recipe.getInstructions()) {
            instruction.setRecipeId(recipe.getId());
            instructionDAO.insert(instruction);
        }

        ContentValues values = new ContentValues();
        values.put(Config.KEY_NAME, recipe.getName());
        values.put(Config.KEY_CATEGORY, recipe.getCategory());
        values.put(Config.KEY_DESCRIPTION, recipe.getDescription());
        values.put(Config.KEY_IMAGE_PATH, recipe.getImagePath());
        values.put(Config.KEY_TIME, recipe.getTime());
        values.put(Config.KEY_MEAL_TYPE, recipe.getMealType());
        db.update(Config.TABLE_NAME, values, Config.KEY_ID + "=" + recipe.getId(), null);
    }

    public void deleteById(long id) {
        ingredientDAO.deleteAllByRecipeId(id);
        instructionDAO.deleteAllByRecipeId(id);
        db.delete(Config.TABLE_NAME, Config.KEY_ID + "=" + id, null);
    }

    public static class Config {
        public static final String TABLE_NAME = "recipes";
        public static final String KEY_ID = "id";
        public static final String KEY_NAME = "name";
        public static final String KEY_CATEGORY = "category";
        public static final String KEY_DESCRIPTION = "description";
        public static final String KEY_IMAGE_PATH = "imagePath";
        public static final String KEY_TIME = "time";
        public static final String KEY_MEAL_TYPE = "mealType";

        public static final String CREATE_TABLE_STATEMENT =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_NAME + " TEXT NOT NULL, " +
                        KEY_CATEGORY + " TEXT NOT NULL, " +
                        KEY_DESCRIPTION + " TEXT NOT NULL, " +
                        KEY_IMAGE_PATH + " TEXT NOT NULL, " +
                        KEY_TIME + " TEXT NOT NULL, " +
                        KEY_MEAL_TYPE + " TEXT NOT NULL)";
    }
}
