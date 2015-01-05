package org.dnd5spellbook;

import android.content.Context;
import android.util.Xml;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.dnd5spellbook.domain.ClassLevelConstraint;
import org.dnd5spellbook.domain.Spell;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Has logic for loading spells from assets
 */
public class SpellLoader {

    private static final Logger logger = Logger.getLogger(SpellLoader.class.getName());

    /**
     * Reads all spells from assets and returns them. The returned list
     * is sorted in alphabetical order for convenience.
     *
     * @param context context to access assets
     * @return list of spells that was read from assets
     */
    public List<Spell> readSpellListFromAssets(Context context) {
        try {
            String[] names = context.getAssets().list(Constants.DND_SPELLS_ASSETS_PATH);
            Multimap<String, ClassLevelConstraint> constraints = readSpellClassLevelConstraints(context);

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
    public Multimap<String, ClassLevelConstraint> readSpellClassLevelConstraints(Context context) {
        XmlPullParser parser = Xml.newPullParser();
        InputStream stream;
        try {
            stream = context.getAssets().open(Constants.DND_SPELLS_ASSETS_PATH + "/spell_metadata.xml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
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
     * @return a Multimap from spell name (key) to class level constraints of the spell (value)
     */
    private Multimap<String, ClassLevelConstraint> readSpellClassLevelConstraints(XmlPullParser parser) throws IOException, XmlPullParserException {
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

    /**
     * Reads the levels sequence {@literal (<level>...</level><level>...</level><level>...</level>)}
     * within one class from  parser and adds constraints to result;
     *
     * @param parser    parser to read xml data from
     * @param result    Multimap to append results to
     * @param className name of the class for creating {@link org.dnd5spellbook.domain.ClassLevelConstraint}
     * @throws IOException            when I/O error occurs
     * @throws XmlPullParserException when xml structure parsing error occurs
     */
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

    /**
     * Reads the items sequence {@literal (<item>...</item><item>...</item><item>...</item>)}
     * within one level from parser and adds constraints to result;
     *
     * @param parser    parser to read xml data from
     * @param result    Multimap to append results to
     * @param className name of the class for creating {@link org.dnd5spellbook.domain.ClassLevelConstraint}
     * @param level     required level for creating {@link org.dnd5spellbook.domain.ClassLevelConstraint}
     * @throws IOException            when I/O error occurs
     * @throws XmlPullParserException when xml structure parsing error occurs
     */
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
}
