// AddEditFragment.java
// Fragment for adding a new recipe or editing an existing one
package com.morrill.recipeapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.morrill.recipeapp.data.DatabaseDescription.Recipe;

public class AddEditFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor>{

    // defines callback method implemented by MainActivity
    public interface AddEditFragmentListener {
        // called when recipe is saved
        void onAddEditCompleted(Uri recipeUri);
    }

    // constant used to identify the Loader
    private static final int RECIPE_LOADER = 0;

    private AddEditFragmentListener listener; // MainActivity
    private Uri recipeUri; // Uri of selected recipe
    private boolean addingNewRecipe = true; // adding (true) or editing

    // EditTexts for recipe information
    private TextInputLayout nameTextInputLayout;
    private TextInputLayout categoryTextInputLayout;
    private TextInputLayout ingredientsTextInputLayout;
    private TextInputLayout instructionsTextInputLayout;
    private FloatingActionButton saveRecipeFAB;

    private CoordinatorLayout coordinatorLayout; // used with SnackBars

    // set AddEditFragmentListener when Fragment attached
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (AddEditFragmentListener) context;
    }

    // remove AddEditFragmentListener when Fragment detached
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    // called when Fragment's view needs to be created
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true); // fragment has menu items to display
        // inflate GUI and get references to EditTexts
        View view =
                inflater.inflate(R.layout.fragment_add_edit, container, false);
        nameTextInputLayout =
                (TextInputLayout) view.findViewById(R.id.nameTextInputLayout);
        nameTextInputLayout.getEditText().addTextChangedListener(
                nameChangedListener);
        categoryTextInputLayout =
                (TextInputLayout) view.findViewById(R.id.categoryTextInputLayout);
        ingredientsTextInputLayout =
                (TextInputLayout) view.findViewById(R.id.ingredientsTextInputLayout);
        instructionsTextInputLayout =
                (TextInputLayout) view.findViewById(R.id.instructionsTextInputLayout);

        // set FloatingActionButton's event listener
        saveRecipeFAB = (FloatingActionButton) view.findViewById(
                R.id.saveFloatingActionButton);
        saveRecipeFAB.setOnClickListener(saveRecipeButtonClicked);
        updateSaveButtonFAB();

        // used to display SnackBars with brief messages
        coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(
                R.id.coordinatorLayout);

        Bundle arguments = getArguments(); // null if creating new recipe

        if (arguments != null) {
            addingNewRecipe = false;
            recipeUri = arguments.getParcelable(MainActivity.RECIPE_URI);
        }

        // if editing an existing recipe, create Loader to get the recipe
        if (recipeUri != null)
            getLoaderManager().initLoader(RECIPE_LOADER, null, this);

        return view;
    }

    // detects when the text in the nameTextInputLayout's EditText changes
    // to hide or show saveButtonFAB
    private final TextWatcher nameChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {}

        // called when the text in nameTextInputLayout changes
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            updateSaveButtonFAB();
        }

        @Override
        public void afterTextChanged(Editable s) { }
    };

    // shows saveButtonFAB only if the name is not empty
    private void updateSaveButtonFAB() {
        String input =
                nameTextInputLayout.getEditText().getText().toString();

        // if there is a name for the recipe, show the FloatingActionButton
        if (input.trim().length() != 0)
            saveRecipeFAB.show();
        else
            saveRecipeFAB.hide();
    }

    // responds to event generated when user saves a recipe
    private final View.OnClickListener saveRecipeButtonClicked =
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // hide the virtual keyboard
                    ((InputMethodManager) getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                            getView().getWindowToken(), 0);
                    saveRecipe(); // save recipe to the database
                }
            };

    // saves recipe information to the database
    private void saveRecipe() {
        // create ContentValues object containing recipe's key-value pairs
        ContentValues contentValues = new ContentValues();
        contentValues.put(Recipe.COLUMN_NAME,
                nameTextInputLayout.getEditText().getText().toString());
        contentValues.put(Recipe.COLUMN_CATEGORY,
                categoryTextInputLayout.getEditText().getText().toString());
        contentValues.put(Recipe.COLUMN_INGREDIENTS,
                ingredientsTextInputLayout.getEditText().getText().toString());
        contentValues.put(Recipe.COLUMN_INSTRUCTIONS,
                instructionsTextInputLayout.getEditText().getText().toString());

        if (addingNewRecipe) {
            // use Activity's ContentResolver to invoke
            // insert on the RecipeAppContentProvider
            Uri newRecipeUri = getActivity().getContentResolver().insert(
                    Recipe.CONTENT_URI, contentValues);

            if (newRecipeUri != null) {
                Snackbar.make(coordinatorLayout,
                        R.string.recipe_added, Snackbar.LENGTH_LONG).show();
                listener.onAddEditCompleted(newRecipeUri);
            }
            else {
                Snackbar.make(coordinatorLayout,
                        R.string.recipe_not_added, Snackbar.LENGTH_LONG).show();
            }
        }
        else {
            // use Activity's ContentResolver to invoke
            // insert on the RecipeAppContentProvider
            int updatedRows = getActivity().getContentResolver().update(
                    recipeUri, contentValues, null, null);

            if (updatedRows > 0) {
                listener.onAddEditCompleted(recipeUri);
                Snackbar.make(coordinatorLayout,
                        R.string.recipe_updated, Snackbar.LENGTH_LONG).show();
            }
            else {
                Snackbar.make(coordinatorLayout,
                        R.string.recipe_not_updated, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    // called by LoaderManager to create a Loader
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // create an appropriate CursorLoader based on the id argument;
        // only one Loader in this fragment, so the switch is unnecessary
        switch (id) {
            case RECIPE_LOADER:
                return new CursorLoader(getActivity(),
                        recipeUri, // Uri of recipe to display
                        null, // null projection returns all columns
                        null, // null selection returns all rows
                        null, // no selection arguments
                        null); // sort order
            default:
                return null;
        }
    }

    // called by LoaderManager when loading completes
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // if the recipe exists in the database, display its data
        if (data != null && data.moveToFirst()) {
            // get the column index for each data item
            int nameIndex = data.getColumnIndex(Recipe.COLUMN_NAME);
            int emailIndex = data.getColumnIndex(Recipe.COLUMN_CATEGORY);
            int streetIndex = data.getColumnIndex(Recipe.COLUMN_INGREDIENTS);
            int cityIndex = data.getColumnIndex(Recipe.COLUMN_INSTRUCTIONS);

            // fill EditTexts with the retrieved data
            nameTextInputLayout.getEditText().setText(
                    data.getString(nameIndex));
            categoryTextInputLayout.getEditText().setText(
                    data.getString(emailIndex));
            ingredientsTextInputLayout.getEditText().setText(
                    data.getString(streetIndex));
            instructionsTextInputLayout.getEditText().setText(
                    data.getString(cityIndex));

            updateSaveButtonFAB();
        }
    }

    // called by LoaderManager when the Loader is being reset
    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}
