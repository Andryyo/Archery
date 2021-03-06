package com.Andryyo.ArchPad.target;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.Andryyo.ArchPad.CShot;
import com.Andryyo.ArchPad.archeryFragment.CArcheryFragment;
import com.Andryyo.ArchPad.archeryFragment.CDistance;
import com.Andryyo.ArchPad.archeryFragment.IOnShotAddListener;

/**
 * Created with IntelliJ IDEA.
 * User: Андрей
 * Date: 10.06.13
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */

public class CEditableTargetView extends CZoomableTargetView {

    private IOnShotAddListener listener;

    public CEditableTargetView(Context context, CDistance distance) {
        super(context, distance);
    }

    public CEditableTargetView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CEditableTargetView(Context context) {
        super(context);
    }

    public void setOnShotAddListener(IOnShotAddListener listener)   {
        this.listener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        super.onTouchEvent(me);
        if (me.getAction()==MotionEvent.ACTION_UP)
        {
            CShot shot = new CShot(getTarget().rings, me.getX() / getCenter() - 1, me.getY() / getCenter() - 1,
                    getArrowRadius());
            getTarget().calcPoints(shot, getArrowRadius());
            listener.addShot(shot);
            cancelDrawSightMark();
            CArcheryFragment.vibrator.vibrate(50);
        }
        if (me.getAction()==MotionEvent.ACTION_MOVE)
        {
            beginDrawSightMark(me.getX(), me.getY());
        }
        return true;
    }

}
