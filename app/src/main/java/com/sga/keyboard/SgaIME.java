package com.sga.keyboard;

import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class SgaIME extends InputMethodService implements KeyboardView.Listener {

    @Override
    public View onCreateInputView() {
        KeyboardView v = new KeyboardView(this);
        v.setListener(this);
        return v;
    }

    @Override
    public void onText(String s) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) ic.commitText(s, 1);
    }

    @Override
    public void onDelete() {
        sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
    }

    @Override
    public void onEnter() {
        EditorInfo ei = getCurrentInputEditorInfo();
        InputConnection ic = getCurrentInputConnection();
        if (ei == null || ic == null) return;
        int action = ei.imeOptions & EditorInfo.IME_MASK_ACTION;
        if ((ei.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
                || action == EditorInfo.IME_ACTION_NONE
                || action == EditorInfo.IME_ACTION_UNSPECIFIED) {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
        } else {
            ic.performEditorAction(action);
        }
    }
}
