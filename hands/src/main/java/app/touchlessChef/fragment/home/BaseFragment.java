package app.touchlessChef.fragment.home;


import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.touchlessChef.constants.RecipeConstants;
import app.touchlessChef.adapter.DatabaseAdapter;
import app.touchlessChef.adapter.recipe.RecipeAdapter;
import app.touchlessChef.model.Recipe;

import app.touchlessChef.R;


public abstract class BaseFragment extends Fragment {
    public interface FragmentListener {
        void onShowRecipe(Recipe recipe, Pair<ImageView, String> pairs);
        void onDeleteRecipe(long recipeId);
    }

    private FragmentListener fragmentListener;
    protected RecyclerView recipeRecyclerView;
    private TextView emptyView;
    protected RecipeAdapter recipeAdapter;
    protected DatabaseAdapter databaseAdapter;
    protected String currentCategory;
    protected List<Recipe> recipes;

    public BaseFragment() {
        recipes = new ArrayList<>();
        databaseAdapter = DatabaseAdapter.getInstance(getActivity());
    }

    public static Fragment newInstance(String category) {
        Fragment fragment;
        if (RecipeConstants.CHINESE.equals(category)) {
            fragment = new ChineseFragment();
        } else {
            fragment = new VietnamFragment();
        }
        Bundle args = new Bundle();
        args.putString("category", category);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(getFragmentLayout(), container, false);

        Bundle args = getArguments();
        assert args != null;
        currentCategory = args.getString("category");
        recipeRecyclerView = rootView.findViewById(R.id.recyclerView);
        emptyView = rootView.findViewById(R.id.empty_view);
        recipeRecyclerView.setHasFixedSize(true);
        recipeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        refresh();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            fragmentListener = (FragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement FragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentListener = null;
    }

    public void refresh() {
        recipes = databaseAdapter.getAllRecipesByCategory(currentCategory);
        toggleEmptyView();
        recipeAdapter = new RecipeAdapter(recipes);
        recipeAdapter.setRecipeListener((recipe, pairs) ->
                fragmentListener.onShowRecipe(recipe, pairs));
        recipeRecyclerView.setAdapter(recipeAdapter);
    }

    private void toggleEmptyView() {
        if (recipes.size() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            recipeRecyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recipeRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    protected abstract @LayoutRes
    int getFragmentLayout();
}