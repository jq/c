package com.ubercalendar.util;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class TouchableWrapper extends FrameLayout {

    public TouchableWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchableWrapper(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TouchableWrapper(Context context) {
        super( context);
        // TODO fix this
        updateMapAfterUserInteraction = (UpdateMapAfterUserInteraction) context;
    }

    private long lastTouched = 0;
    private static final long SCROLL_TIME = 200L; // 200 Milliseconds, but you can adjust that to your liking
    private UpdateMapAfterUserInteraction updateMapAfterUserInteraction;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(getClass().getSimpleName(),"down");
                lastTouched = SystemClock.uptimeMillis();
                break;
            case MotionEvent.ACTION_UP: {
                Log.d(getClass().getSimpleName(),"up");
                long pass = SystemClock.uptimeMillis() - lastTouched;
                if (updateMapAfterUserInteraction != null && (pass > SCROLL_TIME)) {
                    updateMapAfterUserInteraction.onUpdateMapAfterUserInteraction();
                }
            }
            break;
        }
        return super.dispatchTouchEvent(ev);
    }

    // Map Activity must implement this interface
    public interface UpdateMapAfterUserInteraction {
        public void onUpdateMapAfterUserInteraction();
    }

    public void setUpdateMapAfterUserInteraction(UpdateMapAfterUserInteraction mUpdateMapAfterUserInteraction){
        this.updateMapAfterUserInteraction = mUpdateMapAfterUserInteraction;
    }
}