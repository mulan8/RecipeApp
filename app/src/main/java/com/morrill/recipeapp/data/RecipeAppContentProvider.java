// RecipeAppContentProvider.java
// ContentProvider subclass for manipulating the app's database
package com.morrill.recipeapp.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.morrill.recipeapp.R;
import com.morrill.recipeapp.data.DatabaseDescription.Recipe;

public class RecipeAppContentProvider extends ContentProvider {
    // used to access the database
    private RecipeAppDatabaseHelper dbHelper;

    // UriMatcher helps ContentProvider determine operation to perform
    private static final UriMatcher uriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    // constants used with UriMatcher to determine operation to perform
    private static final int ONE_RECIPE = 1; // manipulate one recipe
    private static final int RECIPES = 2; // manipulate recipes table

    // static block to configure this ContentProvider's UriMatcher
    static {
        // Uri for Recipe with the specified id (#)
        uriMatcher.addURI(DatabaseDescription.AUTHORITY,
            Recipe.TABLE_NAME + "/#", ONE_RECIPE);

        // Uri for Recipes table
        uriMatcher.addURI(DatabaseDescription.AUTHORITY,
            Recipe.TABLE_NAME, RECIPES);
    }

    // delete an existing recipe from the database
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int numberOfRowsDeleted;

        switch (uriMatcher.match(uri)) {
            case ONE_RECIPE:
                // get from the uri the id of recipe to update
                String id = uri.getLastPathSegment();

                // delete the recipe
                numberOfRowsDeleted = dbHelper.getWritableDatabase().delete(
                        Recipe.TABLE_NAME, Recipe._ID + "=" + id, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException(
                    getContext().getString(R.string.invalid_delete_uri) + uri);
        }

        // notify observers that the database changed
        if (numberOfRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numberOfRowsDeleted;
    }

    // required method: Not used in this app, so we return null
    @Override
    public String getType(Uri uri) {
        return null;
    }

    // insert a new recipe in the database
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri newRecipeUri = null;

        switch (uriMatcher.match(uri)) {
            case RECIPES:
                // insert the new recipe--success yields new recipe's row id
                long rowId = dbHelper.getWritableDatabase().insert(
                        Recipe.TABLE_NAME, null, values);

                // if the recipe was inserted, create an appropriate Uri;
                // otherwise, throw an exception
                if (rowId > 0) { // SQLite row IDs start at 1
                    newRecipeUri = Recipe.buildRecipeUri(rowId);

                    // notify observers that the database changed
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                else
                    throw new SQLException(
                            getContext().getString(R.string.insert_failed) + uri);
                break;
            default:
                throw new UnsupportedOperationException(
                        getContext().getString(R.string.invalid_insert_uri) + uri);
        }

        return newRecipeUri;
    }

    // called when the RecipeAppContentProvider is created
    @Override
    public boolean onCreate() {
        // create the RecipeAppDatabaseHelper
        dbHelper = new RecipeAppDatabaseHelper(getContext());
        return true; // ContentProvider successfully created
    }

    // query the database
    @Override
    public Cursor query(Uri uri, String[] projection,
                        String selection, String[] selectionArgs, String sortOrder) {

        // create SQLiteQueryBuilder for querying recipes table
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(Recipe.TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case ONE_RECIPE: // recipe with specified id will be selected
                queryBuilder.appendWhere(
                    Recipe._ID + "=" + uri.getLastPathSegment());
                break;
            case RECIPES: // all recipes will be selected
                break;
            default:
                throw new UnsupportedOperationException(
                    getContext().getString(R.string.invalid_query_uri) + uri);
        }

        // execute the query to select one or all recipes
        Cursor cursor = queryBuilder.query(dbHelper.getReadableDatabase(),
            projection, selection, selectionArgs, null, null, sortOrder);

        // configure to watch for content changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    // update an existing recipe in the database
    @Override
    public int update(Uri uri, ContentValues values,
                      String selection, String[] selectionArgs) {
        int numberOfRowsUpdated; // 1 if update successful; 0 o/w

        switch (uriMatcher.match(uri)) {
            case ONE_RECIPE:
                // get from the uri the id of recipe to update
                String id = uri.getLastPathSegment();

                // update the recipe
                numberOfRowsUpdated = dbHelper.getWritableDatabase().update(
                    Recipe.TABLE_NAME, values, Recipe._ID + "=" + id,
                    selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException(
                    getContext().getString(R.string.invalid_query_uri) + uri);
        }

        // if changes were made, notify observers that the database changed
        if (numberOfRowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numberOfRowsUpdated;
    }
}
