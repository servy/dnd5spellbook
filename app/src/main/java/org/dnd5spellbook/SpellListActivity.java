package org.dnd5spellbook;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An activity that displays a list of spells
 */
public class SpellListActivity extends FragmentActivity {

    protected SpellListFragment spellListFragment;

    @Override
    protected void onResume() {
        spellListFragment = (SpellListFragment) getSupportFragmentManager().findFragmentByTag(SpellListFragment.TAG);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        EditText filterText = (EditText) findViewById(R.id.filterText);
        filterText.addTextChangedListener(new FilterTextWatcher());

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
     * Watches for filter text edit changes and invokes spell list filtering
     */
    protected class FilterTextWatcher implements TextWatcher  {
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
            spellListFragment.filter(s);
        }
    }

    /**
     * A main fragment containing a list of spells
     */
    public static class SpellListFragment extends ListFragment implements SwipeListViewTouchListener.OnSwipeCallback {

        public static final String TAG = "SpellListFragment";
        private static final Logger logger = Logger.getLogger(SpellListFragment.class.getName());

        private ArrayAdapter<String> adapter;
        private List<String> fullData;
        private List<String> filteredData;

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

            fullData = readSpellListFromAssets();
            filteredData = new ArrayList<String>(fullData);

            adapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_1, filteredData);
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
            // TODO: add to favorites
        }

        @Override
        public void onSwipeRight(ListView listView, int position) {
            gotoSpellActivity(position);
        }

        /**
         * Filters the displayed list of spells retaining only those which contain
         * {@code filterString}.
         * @param filterString
         */
        public void filter(CharSequence filterString) {
            filteredData.clear();
            if (filterString == null || filterString.length() == 0) {
                filteredData.addAll(fullData);
            }
            else {
                String lowerFilterString = filterString.toString().toLowerCase();
                for (String fullSpell : fullData) {
                    if (fullSpell.toLowerCase().contains(lowerFilterString))
                        filteredData.add(fullSpell);
                }
            }
            adapter.notifyDataSetChanged();
        }

        /**
         * Reads all spell file names from assets and returns them. The returned list
         * is sorted in alphabetical order for convenience.
         *
         * @return List of spell names that was read from assets
         */
        private List<String> readSpellListFromAssets() {
            try {
                String[] names = getActivity().getApplication().getAssets().list(Constants.DND_SPELLS_ASSETS_PATH);

                List<String> results = new ArrayList<String>();
                for (int i=0; i<names.length; i++) {
                    if (names[i].endsWith(".html")) {
                        results.add(names[i].substring(0, names[i].length() - ".html".length()));
                    }
                }

                Collections.sort(results);
                return results;
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error while retrieving spell list", e);
                throw new RuntimeException(e);
            }
        }

        /**
         * Starts an {@link org.dnd5spellbook.SpellActivity} with details of
         * the spell at specified {@code position}.
         *
         * @param position list item index of the spell which details have to be shown
         */
        private void gotoSpellActivity(int position) {
            Intent intent = new Intent(getActivity(), SpellActivity.class);
            intent.putExtra(SpellActivity.SPELL_NAME, filteredData.get(position));
            startActivity(intent);
        }
    }
}
