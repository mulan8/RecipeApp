// DatabaseDescription.java
// Describes the table name and column names for this app's database,
// and other information required by the ContentProvider
package com.morrill.recipeapp.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class DatabaseDescription {
    // ContentProvider's name: typically the package name
    public static final String AUTHORITY =
        "com.morrill.recipeapp.data";

    // base URI used to interact with the ContentProvider
    private static final Uri BASE_CONTENT_URI =
        Uri.parse("content://" + AUTHORITY);

    // nested class defines contents of the recipes table
    public static final class Recipe implements BaseColumns {
        public static final String TABLE_NAME = "recipes"; // table's name

        // Uri for the recipes table
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        // column names for recipes table's columns
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_INGREDIENTS = "ingredients";
        public static final String COLUMN_INSTRUCTIONS = "instructions";

        // creates a Uri for a specific recipe
        public static Uri buildRecipeUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
