package org.dnd5spellbook;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import org.dnd5spellbook.domain.Spell;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * An activity that displays a list of spells
 */
public class SpellListActivity extends FragmentActivity {

    protected SpellListFragment spellListFragment;
    private EditText filterText;
    private CheckBox favOnlyCheckBox;

    // keys for saving properties
    private static final String FILTER = "filter";
    private static final String SHOW_FAV_ONLY = "show_fav_only";
    private static final String FAVORITES = "favorites";

    private static final Set<String> DEFAULT_FAVORITES = new HashSet<>();

    @Override
    protected void onResume() {
        spellListFragment = (SpellListFragment) getSupportFragmentManager().findFragmentByTag(SpellListFragment.TAG);
        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
        filterText.setText(pref.getString(FILTER, ""));
        favOnlyCheckBox.setChecked(pref.getBoolean(SHOW_FAV_ONLY, false));
        spellListFragment.setFavoriteSpellNames(pref.getStringSet(FAVORITES, DEFAULT_FAVORITES));
        spellListFragment.filter(filterText.getText(), favOnlyCheckBox.isChecked());
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(FILTER, filterText.getText().toString());
        editor.putBoolean(SHOW_FAV_ONLY, favOnlyCheckBox.isChecked());
        editor.putStringSet(FAVORITES, spellListFragment.getFavoriteSpellNames());
        editor.apply();
        spellListFragment = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spell_list);

        if (savedInstanceState == null) {
            SpellListFragment fragment = new SpellListFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment, SpellListFragment.TAG)
                    .commit();
        }
        filterText = (EditText) findViewById(R.id.filterText);
        filterText.addTextChangedListener(new FilterTextWatcher());
        favOnlyCheckBox = (CheckBox) findViewById(R.id.favOnlyCheckBox);
        setTitle("Dnd 5 spell list");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.spell_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when "fav only" checkbox state is changed
     *
     * @param view the checkbox that has been clicked
     */
    public void onFavOnlyClicked(View view) {
        spellListFragment.filter(filterText.getText(), favOnlyCheckBox.isChecked());
    }

    /**
     * Watches for filter text edit changes and invokes spell list filtering
     */
    protected class FilterTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // intentionally empty
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // intentionally empty
        }

        @Override
        public void afterTextChanged(Editable s) {
            spellListFragment.filter(s, favOnlyCheckBox.isChecked());
        }
    }

    /**
     * A main fragment containing a list of spells
     */
    public static class SpellListFragment extends ListFragment implements SwipeListViewTouchListener.OnSwipeCallback {

        public static final String TAG = "SpellListFragment";
        private static final Logger logger = Logger.getLogger(SpellListFragment.class.getName());

        private SpellAdapter adapter;
        private SpellLoader spellLoader = new SpellLoader();

        public SpellListFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            adapter = new SpellAdapter(getActivity(), spellLoader.readSpellListFromAssets(getActivity()));
            setListAdapter(adapter);

            new SwipeListViewTouchListener(getListView(), this);
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);
            gotoSpellActivity(position);
        }

        @Override
        public void onSwipeLeft(ListView listView, int position) {
            adapter.setFavorite(position, !adapter.getItem(position).isFavorite());
        }

        @Override
        public void onSwipeRight(ListView listView, int position) {
            gotoSpellActivity(position);
        }

        /**
         * Filters the displayed list of spells retaining only those which contain
         * {@code filterString}.
         *
         * @param filterString string constraining the displayed spell list
         * @param showFavOnly  if true, filters out all nonfavorite items
         */
        public void filter(CharSequence filterString, boolean showFavOnly) {
            adapter.setShowFavOnly(showFavOnly);
            adapter.getFilter().filter(filterString);
        }

        /**
         * Gets a set of names of currently favorite spells
         *
         * @return set of current favorite spell names
         */
        public Set<String> getFavoriteSpellNames() {
            HashSet<String> favorites = new HashSet<>();
            for (Spell spell : adapter)
                if (spell.isFavorite())
                    favorites.add(spell.getName());
            return favorites;
        }

        /**
         * Makes spells which names are in favoriteSpellNames favorite and makes all
         * other spells unfavorite. {@link #filter} method must be called in order to
         * ensure that only rights items are shown after the change.
         *
         * @param favoriteSpellNames set of spell names which should be favorite
         */
        public void setFavoriteSpellNames(Set<String> favoriteSpellNames) {
            for (Spell spell : adapter)
                spell.setFavorite(favoriteSpellNames.contains(spell.getName()));
        }

        /**
         * Starts an {@link org.dnd5spellbook.SpellActivity} with details of
         * the spell at specified {@code position}.
         *
         * @param position list item index of the spell which details have to be shown
         */
        private void gotoSpellActivity(int position) {
            Intent intent = new Intent(getActivity(), SpellActivity.class);
            intent.putExtra(SpellActivity.SPELL_NAME, adapter.getItem(position).getName());
            startActivity(intent);
        }
    }
}
