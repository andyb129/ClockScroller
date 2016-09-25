package uk.co.barbuzz.clockscroller.sample;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import uk.co.barbuzz.clockscroller.DateGetter;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements DateGetter {

    private Context context;
    private List<CalendarTimeSlot> dataSet;

    public RecyclerViewAdapter (List<CalendarTimeSlot> contacts, Context c) {
        this.dataSet = contacts;
        this.context = c;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.element_calendar_row, parent, false);

        return new ContactsViewHolder (view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ContactsViewHolder contactHolder = (ContactsViewHolder) holder;

        contactHolder.time.setText(dataSet.get(position).time);

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    // get scrolling ClockDrawable date from view position
    @Override
    public Date getDateFromAdapter(int pos) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(dataSet.get(pos).time.substring(0,2)));
        cal.set(Calendar.MINUTE, 0);
        return cal.getTime();
    }

    public class ContactsViewHolder extends RecyclerView.ViewHolder {
        TextView time;

        public ContactsViewHolder (View v) {
            super (v);
            time = (TextView) v.findViewById(R.id.element_calendar_row_time_text);
        }
    }
}
