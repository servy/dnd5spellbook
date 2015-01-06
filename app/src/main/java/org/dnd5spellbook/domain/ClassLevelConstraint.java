package org.dnd5spellbook.domain;

/**
 * This class holds pair of values - the name of the caster's class and the level. It
 * defines a limitation for a spell, that is, that spell can be cast by a member of
 * that class at that level.
 */
public class ClassLevelConstraint {
    public ClassName className;
    public int level;

    /**
     * @return name of the casters class that can use the constrained spell
     */
    public ClassName getClassName() {
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
    public ClassLevelConstraint(ClassName className, int level) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassLevelConstraint that = (ClassLevelConstraint) o;

        if (level != that.level) return false;
        if (className != that.className) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = className.hashCode();
        result = 31 * result + level;
        return result;
    }
}
