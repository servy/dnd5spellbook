package org.dnd5spellbook.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * POJO that describes a single spell
 */
public class Spell {
    private String name;
    private boolean favorite;
    private List<ClassLevelConstraint> classLevelConstraints;

    private static class NameComparator implements Comparator<Spell> {
        @Override
        public int compare(Spell lhs, Spell rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    }

    public static Comparator<Spell> NAME_COMPARATOR = new NameComparator();

    /**
     * Creates a new spell with the given name
     *
     * @param name                  name of the spell
     * @param classLevelConstraints class level constraints for this spell
     */
    public Spell(String name, Collection<ClassLevelConstraint> classLevelConstraints) {
        this.name = name;
        this.classLevelConstraints = Collections.unmodifiableList(new ArrayList<>(classLevelConstraints));
    }

    /**
     * @return unmodifiable list of class level constraints for this spell
     */
    public List<ClassLevelConstraint> getClassLevelConstraints() {
        return classLevelConstraints;
    }

    /**
     * @return the name of the spell
     */
    public String getName() {
        return name;
    }

    /**
     * @return true if this spell is favorite by a user and false otherwise
     */
    public boolean isFavorite() {
        return favorite;
    }

    /**
     * Sets whether the spell is a favorite spell
     *
     * @param favorite whether the spell should be marked as favorite or not
     */
    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
