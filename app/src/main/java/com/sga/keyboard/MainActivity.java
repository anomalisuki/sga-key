package com.sga.keyboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        int p = (int) (16 * getResources().getDisplayMetrics().density);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(p, p, p, p);

        TextView info = new TextView(this);
        info.setText("Standard Galactic Alphabet Keyboard\n\n"
                + "1. Aktifkan keyboard ini.\n"
                + "2. Pilih sebagai keyboard aktif.\n"
                + "3. Tombol ⇧ = ganti output antara simbol SGA dan huruf latin biasa.");
        info.setTextSize(16);

        Button enable = new Button(this);
        enable.setText("1. Aktifkan keyboard");
        enable.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));

        Button pick = new Button(this);
        pick.setText("2. Pilih keyboard");
        pick.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showInputMethodPicker();
        });

        EditText test = new EditText(this);
        test.setHint("Coba ketik di sini");

        root.addView(info);
        root.addView(enable);
        root.addView(pick);
        root.addView(test);
        setContentView(root);
    }
}
