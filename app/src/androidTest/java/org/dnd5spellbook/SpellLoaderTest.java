package org.dnd5spellbook;

import android.test.InstrumentationTestCase;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import org.dnd5spellbook.domain.ClassLevelConstraint;
import org.dnd5spellbook.domain.Spell;

import java.util.ArrayList;
import java.util.List;

public class SpellLoaderTest extends InstrumentationTestCase {

    public void testMoreThanZeroSpellsAvailable() throws Exception {
        SpellLoader spellLoader = new SpellLoader();
        List<Spell> spells = spellLoader.readSpellListFromAssets(getInstrumentation().getTargetContext());
        assertTrue(spells.size() > 0);
    }

    public void testConstraintsReferenceExistingSpellsOnly() throws Exception {
        SpellLoader spellLoader = new SpellLoader();
        List<Spell> spells = spellLoader.readSpellListFromAssets(getInstrumentation().getTargetContext());
        Multimap<String, ClassLevelConstraint> constraints = spellLoader.readSpellClassLevelConstraints(getInstrumentation().getTargetContext());

        List<String> failures = new ArrayList<>();
        for (final String name: constraints.keys()) {
            if (!Iterables.any(spells, new Predicate<Spell>() {
                @Override
                public boolean apply(Spell input) {
                    return input.getName().equals(name);
                }
            }))
                failures.add(name);
        }
        assertTrue("Constraints exist for unknown spells: " + failures.toString(), failures.size() == 0);
    }

    public void testEverySpellHasConstraints() {
        SpellLoader spellLoader = new SpellLoader();
        List<Spell> spells = spellLoader.readSpellListFromAssets(getInstrumentation().getTargetContext());
        List<String> failures = new ArrayList<>();
        for (Spell spell: spells)
            if (spell.getClassLevelConstraints().size() == 0)
                failures.add(spell.getName());
        assertTrue("Spells with no constraints: " + failures.toString(), failures.size() == 0);
    }
}