package com.sga.keyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;

/** Keyboard digambar langsung di Canvas: tanpa XML layout, tanpa library, sangat ringan. */
public class KeyboardView extends View {

    public interface Listener {
        void onText(String s);
        void onDelete();
        void onEnter();
    }

    private static final int CHAR = 0, SHIFT = 1, DEL = 2, SYM = 3, SPACE = 4, ENTER = 5;

    // {simbol SGA, huruf latin}
    private static final String[][] R1 = {
        {"ᑑ", "q"}, {"∴", "w"}, {"ᒷ", "e"}, {"∷", "r"}, {"ᒣ", "t"},
        {"॥", "y"}, {"⚍", "u"}, {"╎", "i"}, {"ﾌ", "o"}, {"i!", "p"}};
    private static final String[][] R2 = {
        {"ᖋ", "a"}, {"ϟ", "s"}, {"↸", "d"}, {"⎓", "f"}, {"˧", "g"},
        {"₸", "h"}, {"ⵗ", "j"}, {"ꖌ", "k"}, {"ꖎ", "l"}};
    private static final String[][] R3 = {
        {"ᙁ", "z"}, {"ソ", "x"}, {"ᓵ", "c"}, {"⍊", "v"}, {"ᕊ", "b"},
        {"リ", "n"}, {"ᒲ", "m"}};

    private static class Key {
        final int type;
        final String main;
        final String hint;
        final float weight;
        final RectF cell = new RectF();
        final RectF face = new RectF();

        Key(int type, String main, String hint, float weight) {
            this.type = type;
            this.main = main;
            this.hint = hint;
            this.weight = weight;
        }
    }

    private final ArrayList<ArrayList<Key>> rows = new ArrayList<>();
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float d;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean symbols = false;
    private boolean latin = false; // true = ketikan keluar sebagai huruf latin biasa
    private Key pressed;
    private Listener listener;

    private final Runnable repeat = new Runnable() {
        @Override
        public void run() {
            if (pressed != null && pressed.type == DEL && listener != null) {
                listener.onDelete();
                handler.postDelayed(this, 50);
            }
        }
    };

    public KeyboardView(Context c) {
        super(c);
        d = c.getResources().getDisplayMetrics().density;
        mainPaint.setTextAlign(Paint.Align.CENTER);
        hintPaint.setTextAlign(Paint.Align.RIGHT);
        hintPaint.setTextSize(11 * d);
        build();
    }

    public void setListener(Listener l) {
        listener = l;
    }

    private ArrayList<Key> letterRow(String[][] data) {
        ArrayList<Key> r = new ArrayList<>();
        for (String[] p : data) r.add(new Key(CHAR, p[0], p[1], 1f));
        return r;
    }

    private ArrayList<Key> symRow(String chars) {
        ArrayList<Key> r = new ArrayList<>();
        for (int i = 0; i < chars.length(); i++) {
            r.add(new Key(CHAR, String.valueOf(chars.charAt(i)), null, 1f));
        }
        return r;
    }

    private void build() {
        rows.clear();
        ArrayList<Key> r3;
        if (!symbols) {
            rows.add(letterRow(R1));
            rows.add(letterRow(R2));
            r3 = new ArrayList<>();
            r3.add(new Key(SHIFT, "⇧", null, 1.5f));
            r3.addAll(letterRow(R3));
            r3.add(new Key(DEL, "⌫", null, 1.5f));
        } else {
            rows.add(symRow("1234567890"));
            rows.add(symRow("@#$%&-+()"));
            r3 = new ArrayList<>();
            r3.add(new Key(CHAR, "/", null, 1.5f));
            r3.addAll(symRow("*\"':;!?="));
            r3.add(new Key(DEL, "⌫", null, 1.5f));
        }
        rows.add(r3);
        ArrayList<Key> r4 = new ArrayList<>();
        r4.add(new Key(SYM, symbols ? "ABC" : "?123", null, 1.5f));
        r4.add(new Key(CHAR, ",", null, 1f));
        r4.add(new Key(SPACE, "", null, 5f));
        r4.add(new Key(CHAR, ".", null, 1f));
        r4.add(new Key(ENTER, "⏎", null, 1.5f));
        rows.add(r4);
    }

    @Override
    protected void onMeasure(int wSpec, int hSpec) {
        setMeasuredDimension(MeasureSpec.getSize(wSpec), (int) (4 * 52 * d + 8 * d));
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        arrange();
    }

    private void arrange() {
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;
        float pad = 4 * d, gap = 4 * d;
        float rowH = (h - 2 * pad) / rows.size();
        float unit = (w - 2 * pad) / 10f;
        for (int i = 0; i < rows.size(); i++) {
            ArrayList<Key> row = rows.get(i);
            float sum = 0;
            for (Key k : row) sum += k.weight;
            float x = pad + (10f - sum) * unit / 2f;
            float top = pad + i * rowH;
            for (Key k : row) {
                float wk = k.weight * unit;
                k.cell.set(x, top, x + wk, top + rowH);
                k.face.set(x + gap / 2, top + gap / 2, x + wk - gap / 2, top + rowH - gap / 2);
                x += wk;
            }
        }
    }

    @Override
    protected void onDraw(Canvas c) {
        c.drawColor(0xFF1B1B1F);
        for (ArrayList<Key> row : rows) {
            for (Key k : row) {
                boolean special = k.type != CHAR;
                int color = special ? 0xFF3B3D47 : 0xFF2E2F36;
                if (k.type == SHIFT && latin) color = 0xFF3F5FA8;
                if (k == pressed) color = 0xFF5A5D6B;
                fill.setColor(color);
                c.drawRoundRect(k.face, 8 * d, 8 * d, fill);

                String label = k.main;
                if (k.type == SPACE) label = latin ? "ABC" : "SGA";
                mainPaint.setTextSize((special ? 16 : 22) * d);
                mainPaint.setColor(0xFFFFFFFF);
                Paint.FontMetrics fm = mainPaint.getFontMetrics();
                float cy = k.face.centerY() - (fm.ascent + fm.descent) / 2f;
                c.drawText(label, k.face.centerX(), cy, mainPaint);

                if (k.hint != null) {
                    hintPaint.setColor(latin ? 0xFF8AB4FF : 0xFF9AA0B0);
                    c.drawText(k.hint, k.face.right - 5 * d, k.face.top + 13 * d, hintPaint);
                }
            }
        }
    }

    private Key find(float x, float y) {
        for (ArrayList<Key> row : rows) {
            for (Key k : row) {
                if (k.cell.contains(x, y)) return k;
            }
        }
        return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE: {
                Key k = find(e.getX(), e.getY());
                if (k != pressed) {
                    handler.removeCallbacks(repeat);
                    pressed = k;
                    if (k != null) {
                        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                        if (k.type == DEL && listener != null) {
                            listener.onDelete();
                            handler.postDelayed(repeat, 400);
                        }
                    }
                    invalidate();
                }
                return true;
            }
            case MotionEvent.ACTION_UP: {
                handler.removeCallbacks(repeat);
                Key k = pressed;
                pressed = null;
                if (k != null && k.type != DEL) fire(k);
                invalidate();
                return true;
            }
            case MotionEvent.ACTION_CANCEL:
                handler.removeCallbacks(repeat);
                pressed = null;
                invalidate();
                return true;
            default:
                return true;
        }
    }

    private void fire(Key k) {
        if (listener == null) return;
        switch (k.type) {
            case CHAR:
                listener.onText(latin && k.hint != null ? k.hint : k.main);
                break;
            case SHIFT:
                latin = !latin;
                break;
            case SYM:
                symbols = !symbols;
                build();
                arrange();
                break;
            case SPACE:
                listener.onText(" ");
                break;
            case ENTER:
                listener.onEnter();
                break;
            default:
                break;
        }
    }
}
