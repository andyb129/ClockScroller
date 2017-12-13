package uk.co.barbuzz.clockscroller;

import android.support.v7.widget.RecyclerView;

/**
 * Created by edgar on 5/31/15.
 */
public interface Subscriber {
    void update(RecyclerView rv, float dx, float dy);
}
