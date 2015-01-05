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
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.dnd5spellbook.domain.ClassLevelConstraint;
import org.dnd5spellbook.domain.Spell;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
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

            adapter = new SpellAdapter(getActivity(), readSpellListFromAssets());
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
         * Reads all spells from assets and returns them. The returned list
         * is sorted in alphabetical order for convenience.
         *
         * @return List of spells that was read from assets
         */
        private List<Spell> readSpellListFromAssets() {
            try {
                String[] names = getActivity().getApplication().getAssets().list(Constants.DND_SPELLS_ASSETS_PATH);
                Multimap<String, ClassLevelConstraint> constraints = readSpellClassLevelConstraints();

                List<Spell> results = new ArrayList<>();
                for (String name : names) {
                    if (name.endsWith(".html")) {
                        String spellName = name.substring(0, name.length() - ".html".length());
                        results.add(new Spell(spellName, constraints.get(spellName)));
                    }
                }

                Collections.sort(results, Spell.NAME_COMPARATOR);
                return results;
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error while retrieving spell list", e);
                throw new RuntimeException(e);
            }
        }

        /**
         * Reads all spell constrains from an asset file and returns them as a Multimap.
         * The key in the map is the spell name, and values represent a collection of
         * class level constraints for the spell)
         *
         * @return a Multimap from spell name (key) to class level constraints of the spell (value)
         */
        private Multimap<String, ClassLevelConstraint> readSpellClassLevelConstraints() {
            XmlPullParser parser = Xml.newPullParser();
            InputStream stream;
            try {
                stream = getActivity().getApplicationContext().getAssets().open(Constants.DND_SPELLS_ASSETS_PATH + "/spell_metadata.xml");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try  {
                parser.setInput(stream, "UTF-8");
                return readSpellClassLevelConstraints(parser);
            } catch (IOException | XmlPullParserException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    //noinspection ThrowFromFinallyBlock
                    throw new RuntimeException(e);
                }
            }
        }

        /**
         * Reads all spell constrains from a given parser and returns them as a Multimap.
         * The key in the map is the spell name, and values represent a collection of
         * class level constraints for the spell)
         *
         * @param parser Parser to read constraints data from
         *
         * @return a Multimap from spell name (key) to class level constraints of the spell (value)
         *
         */
        private Multimap<String, ClassLevelConstraint>readSpellClassLevelConstraints(XmlPullParser parser) throws IOException, XmlPullParserException {
            Multimap<String, ClassLevelConstraint> result = HashMultimap.create();
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, null, "classes");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                parser.require(XmlPullParser.START_TAG, null, "class");
                String className = parser.getAttributeValue(null, "name");
                readAndAppendLevels(parser, result, className);
            }
            return result;
        }

        private void readAndAppendLevels(XmlPullParser parser, Multimap<String, ClassLevelConstraint> result, String className) throws IOException, XmlPullParserException {
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                parser.require(XmlPullParser.START_TAG, null, "level");
                int level = Integer.valueOf(parser.getAttributeValue(null, "value"));
                readAndAppendItems(parser, result, className, level);
            }
            parser.require(XmlPullParser.END_TAG, null, "class");
        }

        private void readAndAppendItems(XmlPullParser parser, Multimap<String, ClassLevelConstraint> result, String className, int level) throws IOException, XmlPullParserException {
            ClassLevelConstraint constraint = new ClassLevelConstraint(className, level);
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                parser.require(XmlPullParser.START_TAG, null, "item");
                String spellName = parser.nextText();
                result.put(spellName, constraint);
            }
            parser.require(XmlPullParser.END_TAG, null, "level");
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
