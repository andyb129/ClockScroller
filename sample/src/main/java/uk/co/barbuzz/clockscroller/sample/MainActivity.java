package uk.co.barbuzz.clockscroller.sample;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import uk.co.barbuzz.clockscroller.FastScroller;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mContactsRecyclerView;
    private RecyclerViewAdapter mAdapter;
    private AlertDialog infoDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContactsRecyclerView = findViewById(R.id.contacts_recycler_view);
        mContactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<CalendarTimeSlot> calendarTimeSlotsList = new ArrayList<>();

        for (int hour = 0; hour <= 23; hour++) {
            //String format = new DecimalFormat("##.00").format(hour);
            String format = String.format("%02d:00", hour);
            CalendarTimeSlot calendarTimeSlot = new CalendarTimeSlot(format);
            calendarTimeSlotsList.add(calendarTimeSlot);
        }
        mAdapter = new RecyclerViewAdapter(calendarTimeSlotsList, this);
        mContactsRecyclerView.setAdapter(mAdapter);


        FastScroller fastScroller = findViewById(R.id.fast_scroller_view);

        /*
        //use the following to style the scroll bar & clock handle programmatically if needed
        fastScroller.setClockEdgeColor(getResources().getColor(R.color.clock_edge));
        fastScroller.setClockFaceColor(getResources().getColor(android.R.color.transparent));
        fastScroller.setClockLineWidth(getResources().getDimension(R.dimen.clock_stroke_width));
        fastScroller.setClockScrollBarColor(getResources().getColor(R.color.colorPrimaryDark));
        fastScroller.setClockScrollBarSelectedColor(getResources().getColor(R.color.text_row));
        */

        fastScroller.setRecyclerView(mContactsRecyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_github) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(getResources().getString(R.string.github_link)));
            startActivity(i);
            return true;
        } else if (id == R.id.action_info) {
            showInfoDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showInfoDialog() {
        if (infoDialog != null && infoDialog.isShowing()) {
            //do nothing if already showing
        } else {
            infoDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.info_details)
                    .setCancelable(true)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("More info", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(getResources().getString(R.string.github_link))));
                        }
                    })
                    .create();
            infoDialog.show();
        }
    }
}
