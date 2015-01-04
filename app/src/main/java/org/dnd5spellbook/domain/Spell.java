package org.dnd5spellbook.domain;

import java.util.Comparator;

/**
 * POJO that describes a single spell
 */
public class Spell {
    private String name;
    private boolean favorite;

    private static class NameComparator implements Comparator<Spell> {
        @Override
        public int compare(Spell lhs, Spell rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    }

    public static Comparator<Spell> NAME_COMPARATOR = new NameComparator();

    /**
     * Creates a new spell with the given name
     * @param name the name of the spell
     */
    public Spell(String name) {
        this.name = name;
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
     * @param favorite whether the spell should be marked as favorite or not
     */
    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
