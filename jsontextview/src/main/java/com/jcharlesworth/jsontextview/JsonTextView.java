package com.jcharlesworth.jsontextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonTextView extends View {
    public JsonTextView(Context context) {
        super(context);
        init(null);
    }

    public JsonTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public JsonTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public JsonTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    DrawConfig config;
    int measuredWidth = 0;
    int measuredHeight = 0;

    private void init(AttributeSet attrs) {
        Paint skeletonPaint = new Paint();
        Paint propertyNamePaint;
        Paint propertyValuePaint;
        boolean enquoteStrings = true;
        boolean enquotePropertyNames = false;
        float textSize = 20F;

        if (attrs !=null ) {
            TypedArray attributes = this.getContext().obtainStyledAttributes(attrs, R.styleable.JsonTextView);
            textSize = attributes.getDimension(R.styleable.JsonTextView_textSize, textSize);
            skeletonPaint.setTextSize(textSize);

            if (attributes.hasValue(R.styleable.JsonTextView_propertyNameColor)) {
                propertyNamePaint = new Paint(skeletonPaint);
                propertyNamePaint.setColor(attributes.getColor(R.styleable.JsonTextView_propertyNameColor, 0));
            } else {
                propertyNamePaint = skeletonPaint;
            }

            if (attributes.hasValue(R.styleable.JsonTextView_propertyValueColor)) {
                propertyValuePaint = new Paint(skeletonPaint);
                propertyValuePaint.setColor(attributes.getColor(R.styleable.JsonTextView_propertyValueColor, 0));
            } else {
                propertyValuePaint = skeletonPaint;
            }

            enquoteStrings = attributes.getBoolean(R.styleable.JsonTextView_enquoteStrings, enquoteStrings);
            enquotePropertyNames = attributes.getBoolean(R.styleable.JsonTextView_enquotePropertyNames, enquotePropertyNames);


            skeletonPaint.setColor(attributes.getColor(R.styleable.JsonTextView_textColor, Color.BLACK));


            attributes.recycle();
        } else {
            skeletonPaint.setColor(Color.BLACK);
            propertyNamePaint = skeletonPaint;
            propertyValuePaint = skeletonPaint;
            skeletonPaint.setTextSize(textSize);
        }

        this.config = new DrawConfig(enquoteStrings, enquotePropertyNames, skeletonPaint, propertyNamePaint, propertyValuePaint, textSize, (float)(textSize * 1.2));
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (json != null && !measured) {
            LineLocation loc = new LineLocation(0, 1);
            json.measure(loc, 0, config);
            int totalLines = json.getLineCount();
            measuredHeight = Math.round(totalLines * config.getLineHeightPix());

            int desiredWidth = MeasureSpec.getSize(widthMeasureSpec);
            int actualWidth = Math.round(json.getMaxX());
            measuredWidth = Math.max(desiredWidth, actualWidth);

            measured = true;
        }
        setMeasuredDimension(measuredWidth, measuredHeight);
    }


    private ObjectArea json;
    private boolean measured = false;



    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (json != null) {
            json.draw(canvas, config);
        }
    }

    public void setJson(String jsonString) {
        JsonParser jsonParser = new JsonParser();
        JsonElement parsed = jsonParser.parse(jsonString);
        if (parsed instanceof JsonArray) {
            json = new ArrayObjectArea((JsonArray)parsed);
        } else if (parsed instanceof  JsonObject) {
            json = new NestedObjectArea((JsonObject)parsed);
        } else {
            //TODO: not supported!
        }

        this.requestLayout();
        this.invalidate();
    }

    public static class LineLocation {
        public float x;
        public int line;

        public float getY(DrawConfig config) {
            return this.line * config.getLineHeightPix();
        }

        public LineLocation(float x, int line) {
            this.x = x;
            this.line = line;
        }

        public void set(LineLocation loc) {
            this.x = loc.x;
            this.line = loc.line;
        }

        public void set(float x, int line) {
            this.x = x;
            this.line = line;
        }
    }

    private static class DrawConfig {
        private boolean enquoteStrings = false;
        private boolean enquotePropertyNames = false;
        private Paint skeletonPaint;
        private Paint propertyNamePaint;
        private Paint propertyValuePaint;
        private float incrementSizePix;
        private float lineHeightPix;

        public DrawConfig(boolean enquoteStrings, boolean enquotePropertyNames, Paint skeletonPaint, Paint propertyNamePaint, Paint propertyValuePaint, float incrementSizePix, float lineHeightPix) {
            this.enquoteStrings = enquoteStrings;
            this.enquotePropertyNames = enquotePropertyNames;
            this.skeletonPaint = skeletonPaint;
            this.propertyNamePaint = propertyNamePaint;
            this.propertyValuePaint = propertyValuePaint;
            this.incrementSizePix = incrementSizePix;
            this.lineHeightPix = lineHeightPix;
        }

        public float getIncrementSizePix() {
            return incrementSizePix;
        }

        public float getLineHeightPix() {
            return lineHeightPix;
        }

        public Paint getSkeletonPaint() {
            return skeletonPaint;
        }


        public Paint getPropertyNamePaint() {
            return propertyNamePaint;
        }

        public Paint getPropertyValuePaint() {
            return propertyValuePaint;
        }

        public boolean isEnquoteStrings() {
            return enquoteStrings;
        }

        public boolean isEnquotePropertyNames() {
            return enquotePropertyNames;
        }

    }

    private static abstract class ObjectArea {
        public LineLocation start;
        public LineLocation end;

        public ObjectArea() {
            this.start = new LineLocation(0, 0);
            this.end = new LineLocation(0, 0);
        }

        public abstract void measure(LineLocation start, int level, DrawConfig config);

        public abstract void draw(Canvas canvas, DrawConfig config);

        public abstract int getLineCount();
        public abstract float getMaxX();
    }

    private static class NestedObjectArea extends ObjectArea {
        public LinkedHashSet<Map.Entry<String, ObjectArea>> children;

        public NestedObjectArea(JsonObject gsonObject) {
            this.children = new LinkedHashSet<>();

            Set<Map.Entry<String, JsonElement>> entries = gsonObject.entrySet();
            for (Map.Entry<String, JsonElement> entry : entries) {
                JsonElement value = entry.getValue();
                ObjectArea child = null;
                if (value instanceof JsonObject) {
                    child = new NestedObjectArea((JsonObject) value);
                } else if (value instanceof JsonArray) {
                    child = new ArrayObjectArea((JsonArray) value);
                } else if (value instanceof JsonPrimitive) {
                    child = new ValueObjectArea((JsonPrimitive) value);
                }
                if (child != null) {
                    children.add(new AbstractMap.SimpleEntry<>(entry.getKey(), child));
                }
            }
        }

        @Override
        public void measure(LineLocation start, int level, DrawConfig config) {
            this.start.set(start);
            int propertyLine = start.line + 1;

            for (Map.Entry<String, ObjectArea> property : this.children) {
                ObjectArea child = property.getValue();
                String propertyName = propertyNameToDraw(property.getKey(), config);
                float childX = ((level + 1) * config.getIncrementSizePix()) + config.getSkeletonPaint().measureText(propertyName + ": ");
                LineLocation childLoc = new LineLocation(childX, propertyLine);
                child.measure(childLoc, level + 1, config);
                propertyLine = child.end.line + 1;
            }
            this.end.set(level * config.getIncrementSizePix() + config.getSkeletonPaint().measureText("}"), propertyLine);
        }

        @Override
        public void draw(Canvas canvas, DrawConfig config) {
            canvas.drawText("{", this.start.x, this.start.getY(config), config.getSkeletonPaint());
            for (Map.Entry<String, ObjectArea> property : this.children) {
                ObjectArea child = property.getValue();
                String propertyName = propertyNameToDraw(property.getKey(), config);
                canvas.drawText(propertyName, child.start.x - config.getPropertyNamePaint().measureText(propertyName + ": "), child.start.getY(config), config.getPropertyNamePaint());
                canvas.drawText(": ", child.start.x - config.getPropertyNamePaint().measureText(": "), child.start.getY(config), config.getSkeletonPaint());
                child.draw(canvas, config);
            }
            canvas.drawText("}", this.end.x - config.getSkeletonPaint().measureText("}"), this.end.getY(config), config.getSkeletonPaint());
        }

        public String propertyNameToDraw(String propertyName, DrawConfig config) {
            return config.isEnquotePropertyNames() ? "\"" + propertyName + "\"" : propertyName;
        }


        @Override
        public int getLineCount() {
            int itemSum = 0;
            for (Map.Entry<String, ObjectArea> property : this.children) {
                ObjectArea child = property.getValue();
                itemSum += child.getLineCount();
            }
            return itemSum + 2;
        }

        @Override
        public float getMaxX() {
            float x = Math.max(this.start.x, this.end.x);
            for (Map.Entry<String, ObjectArea> property : this.children) {
                ObjectArea child = property.getValue();
                x = Math.max(x, child.getMaxX());
            }
            return x;
        }
    }

    private static class ValueObjectArea extends ObjectArea {
        public String value;
        public boolean isString;

        public ValueObjectArea(JsonPrimitive jsonPrimitive) {
            this.value = jsonPrimitive.getAsString();
            this.isString = jsonPrimitive.isString();
        }

        @Override
        public void measure(LineLocation start, int level, DrawConfig config) {
            this.start.set(start);
            this.end.set(start.x + config.getSkeletonPaint().measureText(this.valueToDraw(config)), start.line);
        }

        public String valueToDraw(DrawConfig config) {
            return config.isEnquoteStrings() && this.isString ? "\"" + this.value + "\"" : this.value;
        }

        @Override
        public void draw(Canvas canvas, DrawConfig config) {
            canvas.drawText(this.valueToDraw(config), this.start.x, this.start.getY(config), config.getPropertyValuePaint());
        }

        @Override
        public int getLineCount() {
            return 1;
        }

        @Override
        public float getMaxX() {
            return this.end.x;
        }
    }

    private static class ArrayObjectArea extends ObjectArea {
        public List<ObjectArea> items;

        public ArrayObjectArea(JsonArray gsonArray) {
            this.items = new ArrayList<>();

            int arraySize = gsonArray.size();
            for (int i = 0; i < arraySize; i++) {
                JsonElement value = gsonArray.get(i);
                ObjectArea child = null;
                if (value instanceof JsonObject) {
                    child = new NestedObjectArea((JsonObject) value);
                } else if (value instanceof JsonArray) {
                    child = new ArrayObjectArea((JsonArray) value);
                } else if (value instanceof JsonPrimitive) {
                    child = new ValueObjectArea((JsonPrimitive) value);
                }
                if (child != null) {
                    items.add(child);
                }
            }
        }

        @Override
        public void measure(LineLocation start, int level, DrawConfig config) {
            this.start.set(start);
            if (this.items.size() > 0) {
                int itemLine = start.line + 1;
                for (ObjectArea item : this.items) {
                    LineLocation itemLoc = new LineLocation((level + 1) * config.getIncrementSizePix(), itemLine);
                    item.measure(itemLoc, level + 1, config);
                    itemLine = item.end.line + 1;
                }
                LineLocation lastChildEnd = this.items.get(this.items.size() - 1).end;
                this.end.set(lastChildEnd.x + config.getSkeletonPaint().measureText("]"), lastChildEnd.line);
            } else {
                this.end.set(start.x + config.skeletonPaint.measureText("[]"), start.line);
            }
        }

        @Override
        public void draw(Canvas canvas, DrawConfig config) {
            canvas.drawText("[", this.start.x, this.start.getY(config), config.getSkeletonPaint());
            for(ObjectArea item : this.items) {
                item.draw(canvas, config);
                if (item != this.items.get(this.items.size() - 1)) {
                    canvas.drawText(",", item.end.x, item.end.getY(config), config.getSkeletonPaint());
                }
            }
            canvas.drawText("]", this.end.x - config.getSkeletonPaint().measureText("]"), this.end.getY(config), config.getSkeletonPaint());
        }

        @Override
        public int getLineCount() {
            int itemSum = 0;
            for(ObjectArea item : this.items) {
                itemSum += item.getLineCount();
            }
            return itemSum + 1;
        }

        @Override
        public float getMaxX() {
            float x = Math.max(this.start.x, this.end.x);
            for(ObjectArea item : this.items) {
                x = Math.max(x, item.getMaxX());
            }
            return x;
        }
    }

}
