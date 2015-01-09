package org.dnd5spellbook.domain;

import org.dnd5spellbook.R;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * ClassName have all possible dnd classes
 */
public enum ClassName {
    BARD("Bard", "bard", R.color.bard),
    CLERIC("Cleric", "cleric", R.color.cleric),
    DRUID("Druid", "druid", R.color.druid),
    PALADIN("Paladin", "paladin", R.color.paladin),
    RANGER("Ranger", "ranger", R.color.ranger),
    SORCERER("Sorcerer", "sorcerer", R.color.sorcerer),
    WARLOCK("Warlock", "warlock", R.color.warlock),
    WIZARD("Wizard", "wizard", R.color.wizard);

    private final String displayName;
    private final String uniqueName;
    private final int colorId;

    ClassName(String displayName, String uniqueName, int colorId) {
        this.displayName = displayName;
        this.uniqueName = uniqueName;
        this.colorId = colorId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public int getColorId() {
        return colorId;
    }

    /**
     * Gets the ClassName instance by its unique string representation
     *
     * @param classUniqueName string representation of the class name
     * @return ClassName instance which uniqueName equals (ignore case) to classUniqueName
     * @throws java.lang.IllegalArgumentException if no ClassName instance has
     *                                            uniqueName that equals (ignore case) to
     *                                            classUniqueName
     */
    public static ClassName fromString(String classUniqueName) {
        String lowercasedClassName = classUniqueName.toLowerCase();
        for (ClassName c : values())
            if (c.getUniqueName().toLowerCase().equals(lowercasedClassName))
                return c;
        throw new IllegalArgumentException("No ClassName enum for " + classUniqueName);
    }

    /**
     * Converts a set of class name values to a set of strings. Intended for serialization.
     *
     * @param classNames collection of class names to be converted to strings
     * @return set of strings that contain all the class names from classNames
     */
    public static Set<String> toStringSet(Collection<ClassName> classNames) {
        Set<String> result = new HashSet<>();
        for (ClassName c : classNames)
            result.add(c.getUniqueName());
        return result;
    }

    /**
     * Converts a collection of class name strings to a set of ClassName. Intended for deserialization.
     *
     * @param classNames collection of strings which are class names
     * @return set of ClassName that contain all ClassName instances with names matching classNames
     */
    public static Set<ClassName> fromStringCollection(Collection<String> classNames) {
        Set<ClassName> result = EnumSet.noneOf(ClassName.class);
        for (String s : classNames)
            result.add(fromString(s));
        return result;
    }


}
