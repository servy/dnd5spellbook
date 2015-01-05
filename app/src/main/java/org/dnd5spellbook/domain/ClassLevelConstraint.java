package org.dnd5spellbook.domain;

/**
 * This class holds pair of values - the name of the caster's class and the level. It
 * defines a limitation for a spell, that is, that spell can be cast by a member of
 * that class at that level.
 */
public class ClassLevelConstraint {
    public String className;
    public int level;

    /**
     * @return name of the casters class that can use the constrained spell
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return level required for a member of a class to cast the constrained spell
     */
    public int getLevel() {
        return level;
    }

    /**
     * Creates a class-level constraint for a spell.
     *
     * @param className name of the casters class that can use the constrained spell
     * @param level     level required for a member of a class to cast the constrained spell
     */
    public ClassLevelConstraint(String className, int level) {
        this.className = className;
        this.level = level;
    }

    @Override
    public String toString() {
        return "ClassLevelConstraint{" +
                "className='" + className + '\'' +
                ", level=" + level +
                '}';
    }
}
