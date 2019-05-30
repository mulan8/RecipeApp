// RecipeAppDatabaseHelper.java
// SQLiteOpenHelper subclass that defines the app's database
package com.morrill.recipeapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.morrill.recipeapp.data.DatabaseDescription.Recipe;

class RecipeAppDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "AddressBook.db";
    private static final int DATABASE_VERSION = 1;

    // constructor
    public RecipeAppDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // creates the recipes table when the database is created
    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL for creating the recipes table
        final String CREATE_RECIPES_TABLE =
                "CREATE TABLE " + Recipe.TABLE_NAME + "(" +
                        Recipe._ID + " integer primary key, " +
                        Recipe.COLUMN_NAME + " TEXT, " +
                        Recipe.COLUMN_CATEGORY + " TEXT, " +
                        Recipe.COLUMN_INGREDIENTS + " TEXT, " +
                        Recipe.COLUMN_INSTRUCTIONS + " TEXT);";
        db.execSQL(CREATE_RECIPES_TABLE); // create the recipes table
    }

    // normally defines how to upgrade the database when the schema changes
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) { }
}
