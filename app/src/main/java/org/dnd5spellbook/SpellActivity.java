package org.dnd5spellbook;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import java.util.logging.Logger;

/**
 * An activity with a spell details information
 */
public class SpellActivity extends ActionBarActivity {

    public final static String SPELL_NAME = "org.dnd5spellbook.SPELL_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spell);
        SpellFragment spellFragment;
        if (savedInstanceState == null) {
            spellFragment = new SpellFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, spellFragment)
                    .commit();
        }
        else
            spellFragment = (SpellFragment)getSupportFragmentManager().findFragmentById(R.id.container);

        String spellName = getIntent().getStringExtra(SPELL_NAME);

        spellFragment.setSpellName(spellName);
        setTitle(spellName);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.spell, menu);
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
     * A fragment containing spell information
     */
    public static class SpellFragment extends Fragment {

        private static final Logger logger = Logger.getLogger(SpellFragment.class.getName());

        private String spellName;
        private WebView webview;

        public SpellFragment() {
        }

        private void loadData() {
            String fullPath = "file:///android_asset/" + Constants.DND_SPELLS_ASSETS_PATH + "/" +
                    spellName + ".html";
            logger.info("loading data from " + fullPath);
            webview.loadUrl(fullPath);
        }

        /**
         * Sets the spell name that corresponds to the file name of a file in assets/dndbundle
         * containing spells details information. If this method is invoked after view creation
         * it will force loading the information in the view; otherwise, the information will be
         * loaded as soon as view is created.
         *
         * @param spellName pell name that corresponds to the file name of a file in
         *                  assets/dndbundle
         */
        public void setSpellName(String spellName) {
            this.spellName = spellName;
            if (webview != null)
                loadData();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_spell, container, false);
            webview = (WebView) rootView.findViewById(R.id.webview);
            if (spellName != null)
                loadData();

            return rootView;
        }
    }
}
