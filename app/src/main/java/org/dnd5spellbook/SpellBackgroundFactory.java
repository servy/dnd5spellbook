package org.dnd5spellbook;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;

import org.dnd5spellbook.domain.ClassLevelConstraint;
import org.dnd5spellbook.domain.ClassName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * Generates background drawables for list items by given {@link org.dnd5spellbook.domain.ClassLevelConstraint}'s
 */
public class SpellBackgroundFactory {

    private final Map<Set<ClassLevelConstraint>, Drawable> cashe = new HashMap<>();

    private static final Logger logger = Logger.getLogger(SpellBackgroundFactory.class.getName());

    /**
     * Gets the background for a list item by looking at the constraints
     *
     * @param constraints the constraints that define the background
     * @param context application context to get resources from
     * @return Drawable to be used as a background for a list item
     */
    public Drawable getBackground(final Context context, Collection<ClassLevelConstraint> constraints) {
        SortedSet<ClassLevelConstraint> constraintSet = new TreeSet<>(new Comparator<ClassLevelConstraint>() {
            @Override
            public int compare(ClassLevelConstraint lhs, ClassLevelConstraint rhs) {
                int r = Integer.compare(lhs.getClassName().ordinal(), rhs.getClassName().ordinal());
                if (r != 0)
                    return r;
                return Integer.compare(lhs.getLevel(), rhs.getLevel());
            }
        });
        constraintSet.addAll(constraints);

        if (cashe.containsKey(constraintSet)) {
            return cashe.get(constraintSet);
        }

        Drawable result = new ClassInfoDrawable(context, constraintSet);
        cashe.put(constraintSet, result);
        return result;
    }

    private final static Map<ClassName, Paint> classNamePaintMap = new HashMap<>();
    private static Paint getPaintByClassName(ClassName className, Context context) {
        if (classNamePaintMap.containsKey(className))
            return classNamePaintMap.get(className);
        Paint paint = new Paint();
        paint.setColor(context.getResources().getColor(className.getColorId()));
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(204);
        classNamePaintMap.put(className, paint);
        return paint;
    }

    private class ClassInfoDrawable extends PaintDrawable {
        private final float density;
        private Collection<ClassLevelConstraint> constraints;
        private List<Paint> paintList = new ArrayList<>();
        private final Paint textPaint = new Paint();
        private final Paint paintStroke = new Paint();
        private float padding;
        private int dpPadding;

        private ClassInfoDrawable(Context context, Collection<ClassLevelConstraint> constraints) {
            this.constraints = constraints;
            this.density = context.getResources().getDisplayMetrics().density;
            setDpPadding(3);
            for (ClassLevelConstraint c: constraints) {
                paintList.add(getPaintByClassName(c.getClassName(), context));
            }
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setColor(context.getResources().getColor(R.color.level_text));
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setAlpha(204);
            paintStroke.setStyle(Paint.Style.STROKE);
            paintStroke.setColor(context.getResources().getColor(R.color.level_square_outline));
            paintStroke.setStrokeWidth(1 * density);
            paintStroke.setTextAlign(Paint.Align.CENTER);
            paintStroke.setAlpha(204);
        }

        public int getDpPadding() {
            return dpPadding;
        }

        public void setDpPadding(int dpPadding) {
            this.dpPadding = dpPadding;
            this.padding = dpPadding * density;
        }

        @Override
        public void draw(Canvas canvas) {
            final Rect boundsRect = getBounds();
            float squareSize = boundsRect.height() - 2 * padding;
            float left = boundsRect.width() - padding - squareSize;

            Iterator<Paint> paintIterator = paintList.iterator();
            Iterator<ClassLevelConstraint> constraintIterator = constraints.iterator();
            textPaint.setTextSize((float) (squareSize * 0.75));
            paintStroke.setTextSize((float) (squareSize * 0.75));
            float textBaseline = (float) (boundsRect.bottom - squareSize * 0.25);
            float roundRadius = density * 3;
            while (paintIterator.hasNext() && constraintIterator.hasNext()) {
                final RectF r = new RectF(left, padding, left + squareSize, padding + squareSize);
                canvas.drawRoundRect(r, roundRadius, roundRadius, paintIterator.next());
                canvas.drawRoundRect(r, roundRadius, roundRadius, paintStroke);

                String text = String.valueOf(constraintIterator.next().getLevel());
                canvas.drawText(text, r.centerX(), textBaseline, textPaint);
                left -= squareSize + padding;
            }
        }
    }
}
