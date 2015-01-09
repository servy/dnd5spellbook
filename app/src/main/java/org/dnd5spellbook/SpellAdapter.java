package org.dnd5spellbook;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.dnd5spellbook.domain.ClassLevelConstraint;
import org.dnd5spellbook.domain.ClassName;
import org.dnd5spellbook.domain.Spell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Adapter to use spells in a list view
 */
public class SpellAdapter extends BaseAdapter implements Filterable, Iterable<Spell> {
    private Activity context;
    private List<Spell> originalValues;
    private List<Spell> filteredValues;
    private SpellFilter spellFilter;
    private Set<ClassName> classNamesFilter = EnumSet.allOf(ClassName.class);
    private Set<Integer> levelFilter = new HashSet<>(10);

    {
        for (int i = 0; i < 10; i++)
            levelFilter.add(i);
    }

    private boolean showFavOnly;

    private static final SpellBackgroundFactory spellBackgroundFactory = new SpellBackgroundFactory();

    public SpellAdapter(Activity context, List<Spell> values) {
        this.filteredValues = new ArrayList<>(values);
        this.originalValues = values;
        this.context = context;
    }

    /**
     * @return true if list is showing only favorite spells
     */
    public boolean isShowFavOnly() {
        return showFavOnly;
    }

    /**
     * Sets whether the list should show only favorite spells. Takes effect only on the next
     * filtering. You can trigger filtering by calling {@code getFilter().filter()}
     *
     * @param showFavOnly whether only favorite spells should be shown
     */
    public void setShowFavOnly(boolean showFavOnly) {
        this.showFavOnly = showFavOnly;
    }

    /**
     * Sets the set of classes to constrain the spell list; only spells of those classes
     * are shown. Takes effect only on the next filtering. You can trigger filtering by
     * calling {@code getFilter().filter()}
     *
     * @param classNamesFilter set of class names that will limit shown spells
     */
    public void setClassNamesFilter(Set<ClassName> classNamesFilter) {
        this.classNamesFilter = EnumSet.copyOf(classNamesFilter);
    }

    /**
     * Gets current set of classes that limits shown spells
     *
     * @return current set of classes that limits shown spells
     */
    public Set<ClassName> getClassNamesFilter() {
        return classNamesFilter;
    }

    /**
     * Sets the set of levels to constrain the spell list; only spells of those
     * levels are shown. Takes effect only on the next filtering. You can trigger filtering by
     * calling {@code getFilter().filter()}
     *
     * @param levelFilter set of levels that will limit shown spells
     */
    public void setLevelFilter(Set<Integer> levelFilter) {
        this.levelFilter = new HashSet<>(levelFilter);
    }

    /**
     * Gets current set of levels that limits shown spells
     *
     * @return current set of levels that limits shown spells
     */
    public Set<Integer> getLevelFilter() {
        return levelFilter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        return filteredValues.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Spell getItem(int position) {
        return filteredValues.get(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        public TextView label;
        public ImageView image;

        private ViewHolder(TextView label, ImageView image) {
            this.label = label;
            this.image = image;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Spell> iterator() {
        return Collections.unmodifiableCollection(filteredValues).iterator();
    }

    /**
     * Marks the spell at a given position as favorite or clears the favorite mark
     *
     * @param position position of the spell to change the favorite status
     * @param favorite whether spell should be favorite or not
     */
    public void setFavorite(int position, boolean favorite) {
        getItem(position).setFavorite(favorite);
        notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        View rowView = convertView;
        if (rowView == null) {
            rowView = context.getLayoutInflater().inflate(R.layout.spell_list_item, null);
            viewHolder = new ViewHolder((TextView) rowView.findViewById(R.id.label), (ImageView) rowView.findViewById(R.id.icon));
            rowView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) rowView.getTag();

        Spell spell = getItem(position);
        viewHolder.label.setText(spell.getName());
        if (spell.isFavorite())
            viewHolder.image.setImageResource(android.R.drawable.btn_star_big_on);
        else
            viewHolder.image.setImageResource(android.R.color.transparent);

        try {
            rowView.setBackground(spellBackgroundFactory.getBackground(context.getApplication(), spell.getClassLevelConstraints()));
        } catch (Exception e) {
            throw new RuntimeException("Error with spell " + spell, e);
        }

        return rowView;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Filter getFilter() {
        if (spellFilter == null) {
            spellFilter = new SpellFilter();
        }
        return spellFilter;
    }

    private class SpellFilter extends Filter {

        private boolean matchLevelFilter(Spell spell) {
            return Iterables.any(spell.getClassLevelConstraints(), new Predicate<ClassLevelConstraint>() {
                @Override
                public boolean apply(ClassLevelConstraint x) {
                    return levelFilter.contains(x.getLevel());
                }
            });
        }

        private boolean matchClassNameFilter(Spell spell) {
            return Iterables.any(spell.getClassLevelConstraints(), new Predicate<ClassLevelConstraint>() {
                @Override
                public boolean apply(ClassLevelConstraint x) {
                    return classNamesFilter.contains(x.getClassName());
                }
            });
        }

        private boolean matchFavOnlyFilter(Spell spell) {
            if (!showFavOnly)
                return true;
            return  spell.isFavorite();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<Spell> newValues = new ArrayList<>();
            if (constraint == null)
                constraint = "";
            final String lowerFilterString = constraint.toString().toLowerCase();
            for (Spell spell : originalValues) {
                if (spell.getName().toLowerCase().contains(lowerFilterString)) {
                    if (matchFavOnlyFilter(spell) && matchClassNameFilter(spell) && matchLevelFilter(spell))
                        newValues.add(spell);
                }
            }

            Collections.sort(newValues, new Comparator<Spell>() {
                @Override
                public int compare(Spell lhs, Spell rhs) {
                    int leftBestFit = lhs.getName().toLowerCase().startsWith(lowerFilterString) ? 0 : 1;
                    int rightBestFit = rhs.getName().toLowerCase().startsWith(lowerFilterString) ? 0 : 1;
                    int result = leftBestFit - rightBestFit;
                    if (result != 0)
                        return result;
                    return lhs.getName().compareTo(rhs.getName());
                }
            });
            results.values = newValues;
            results.count = newValues.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            filteredValues = (List<Spell>) results.values;
            notifyDataSetChanged();
        }
    }


}
