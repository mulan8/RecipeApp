// MainActivity.java
// Hosts the app's fragments and handles communication between them
package com.morrill.recipeapp;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity
    implements RecipesFragment.RecipesFragmentListener,
    DetailFragment.DetailFragmentListener,
    AddEditFragment.AddEditFragmentListener {

    // key for storing a recipe's Uri in a Bundle passed to a fragment
    public static final String RECIPE_URI = "recipe_uri";

    private RecipesFragment recipesFragment; // displays recipe list

    // display RecipesFragment when MainActivity first loads
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // if layout contains fragmentContainer, the phone layout is in use;
        // create and display a RecipesFragment
        if (savedInstanceState == null &&
                findViewById(R.id.fragmentContainer) != null) {
            // create RecipesFragment
            recipesFragment = new RecipesFragment();
            // add the fragment to the FrameLayout
            FragmentTransaction transaction =
                    getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragmentContainer, recipesFragment);
            transaction.commit(); // display RecipesFragment
        }
        else {
            recipesFragment =
                    (RecipesFragment) getSupportFragmentManager().
                            findFragmentById(R.id.recipesFragment);
        }
    }

    // display DetailFragment for selected recipe
    @Override
    public void onRecipeSelected(Uri recipeUri) {
        if (findViewById(R.id.fragmentContainer) != null) // phone
            displayRecipe(recipeUri, R.id.fragmentContainer);
        else { // tablet
            // removes top of back stack
            getSupportFragmentManager().popBackStack();

            displayRecipe(recipeUri, R.id.rightPaneContainer);
        }
    }

    // display AddEditFragment to add a new recipe
    @Override
    public void onAddRecipe() {
        if (findViewById(R.id.fragmentContainer) != null) // phone
            displayAddEditFragment(R.id.fragmentContainer, null);
        else // tablet
            displayAddEditFragment(R.id.rightPaneContainer, null);
    }

    // display a recipe
    private void displayRecipe(Uri recipeUri, int viewID) {
        DetailFragment detailFragment = new DetailFragment();

        // specify recipe's Uri as an argument to the DetailFragment
        Bundle arguments = new Bundle();
        arguments.putParcelable(RECIPE_URI, recipeUri);
        detailFragment.setArguments(arguments);

        // use a FragmentTransaction to display the DetailFragment
        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();
        transaction.replace(viewID, detailFragment);
        transaction.addToBackStack(null);
        transaction.commit(); // causes DetailFragment to display
    }

    // display fragment for adding a new or editing an existing recipe
    private void displayAddEditFragment(int viewID, Uri recipeUri) {
        AddEditFragment addEditFragment = new AddEditFragment();

        // if editing existing recipe, provide recipeUri as an argument
        if (recipeUri != null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(RECIPE_URI, recipeUri);
            addEditFragment.setArguments(arguments);
        }

        // use a FragmentTransaction to display the AddEditFragment
        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();
        transaction.replace(viewID, addEditFragment);
        transaction.addToBackStack(null);
        transaction.commit(); // causes AddEditFragment to display
    }

    // return to recipe list when displayed recipe deleted
    @Override
    public void onRecipeDeleted() {
        // removes top of back stack
        getSupportFragmentManager().popBackStack();
        recipesFragment.updateRecipeList(); // refresh recipes
    }

    // display the AddEditFragment to edit an existing recipe
    @Override
    public void onEditRecipe(android.net.Uri recipeUri) {
        if (findViewById(R.id.fragmentContainer) != null) // phone
            displayAddEditFragment(R.id.fragmentContainer, recipeUri);
        else // tablet
            displayAddEditFragment(R.id.rightPaneContainer, recipeUri);
    }

    // update GUI after new recipe or updated recipe saved
    @Override
    public void onAddEditCompleted(android.net.Uri recipeUri) {
        // removes top of back stack
        getSupportFragmentManager().popBackStack();
        recipesFragment.updateRecipeList(); // refresh recipes

        if (findViewById(R.id.fragmentContainer) == null) { // tablet
            // removes top of back stack
            getSupportFragmentManager().popBackStack();

            // on tablet, display recipe that was just added or edited
            displayRecipe(recipeUri, R.id.rightPaneContainer);
        }
    }
}
