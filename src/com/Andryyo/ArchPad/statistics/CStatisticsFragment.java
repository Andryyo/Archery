package com.Andryyo.ArchPad.statistics;

import java.util.Arrays;
import java.util.Calendar;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.*;
import android.widget.*;
import com.Andryyo.ArchPad.CShot;
import com.Andryyo.ArchPad.R;
import com.Andryyo.ArchPad.archeryFragment.CDistance;
import com.Andryyo.ArchPad.database.CSQLiteOpenHelper;

public class CStatisticsFragment extends Fragment {

	private ExpandListAdapter adapter;
	private ExpandableListView expandableListView;
    private Context context;

    //TODO:Поработать над статистикой:подсчёт очков, графики т.д.

    public void update()    {
        adapter.update();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
        setHasOptionsMenu(true);
        adapter = new ExpandListAdapter(getLoaderManager(),context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)  {
        expandableListView = new ExpandableListView(context);
        expandableListView.setAdapter(adapter);
        registerForContextMenu(expandableListView);
        return expandableListView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_display, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId())
    	{
    	case R.id.clear:
    	{
			adapter.deleteAllDistances();
	    	return true;
    	}
    	}
    	return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu,View view,ContextMenu.ContextMenuInfo contextMenuInfo)   {
        super.onCreateContextMenu(contextMenu,view,contextMenuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.records_context_menu,contextMenu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId())
        {
            case R.id.delete_record:
            {
                ExpandableListView.ExpandableListContextMenuInfo info =
                    (ExpandableListView.ExpandableListContextMenuInfo)menuItem.getMenuInfo();
                adapter.deleteRound(adapter.getGroupId(ExpandableListView.getPackedPositionGroup(info.packedPosition)));
                return true;
            }
            case  R.id.view_record:
            {
                ExpandableListView.ExpandableListContextMenuInfo info =
                        (ExpandableListView.ExpandableListContextMenuInfo)menuItem.getMenuInfo();
                CRecordViewFragment fragment = new CRecordViewFragment(info.id);
                fragment.show(getFragmentManager().beginTransaction(), "recordViewDialog");
                return true;
            }
        }
        return false;
    }

    private class ExpandListAdapter extends CursorTreeAdapter implements LoaderManager.LoaderCallbacks<Cursor>{

        private static final String GROUP_POS = "groupNum";
        CSQLiteOpenHelper helper;
        int sum;
        LoaderManager loaderManager;

        public ExpandListAdapter(LoaderManager loaderManager, Context context) {
            super(null, context, true);
            helper = CSQLiteOpenHelper.getHelper(context);
            this.loaderManager = loaderManager;
            update();
        }

        public void update()    {
            loaderManager.restartLoader(0, null, this);
        }

        private void fillStatisticsBlockView(LinearLayout layout, CShot[][] ends)  {
            int first_ends_sum = 0;
            int second_ends_sum = 0;
            for (CShot shot : ends[0])
                if (shot!=null)
                    first_ends_sum+=shot.getPoints();
            if (ends[1]!=null)
                for (CShot shot : ends[1])
                    if (shot!=null)
                        second_ends_sum+=shot.getPoints();
            CBorderedTextView tv = (CBorderedTextView)layout.findViewById(R.id.first_ends);
            tv.setText(Arrays.deepToString(ends[0]));
            if (ends[1]!=null)
                {
                tv = (CBorderedTextView)layout.findViewById(R.id.second_ends);
                tv.setText(Arrays.deepToString(ends[1]));
                tv = (CBorderedTextView)layout.findViewById(R.id.second_ends_sum);
                tv.setText(Integer.toString(second_ends_sum));
                }
            tv = (CBorderedTextView)layout.findViewById(R.id.first_ends_sum);
            tv.setText(Integer.toString(first_ends_sum));
            tv = (CBorderedTextView)layout.findViewById(R.id.two_ends);
            tv.setText(Integer.toString((first_ends_sum+second_ends_sum)));
            tv = (CBorderedTextView)layout.findViewById(R.id.all_ends);
            sum+=first_ends_sum+second_ends_sum;
            tv.setText(Integer.toString(sum));
        }

        public void deleteRound(long _id)    {
            CSQLiteOpenHelper.getHelper(context).deleteRound(_id);
            update();
        }

        public void deleteAllDistances()    {
            CSQLiteOpenHelper.getHelper(context).deleteAllRounds();
            update();
        }

        protected Cursor getChildrenCursor(Cursor groupCursor) {
            Bundle bundle = new Bundle();
            bundle.putInt(GROUP_POS, groupCursor.getPosition());
            loaderManager.restartLoader((int) groupCursor.getLong(groupCursor.getColumnIndex("_id")), bundle, this);
            return null;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            if (i==0)
                return CSQLiteOpenHelper.getCursorLoader(context, CSQLiteOpenHelper.TABLE_ROUNDS);
            else
                return CSQLiteOpenHelper.getRoundCursorLoader(context, i, bundle);

        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            if (cursorLoader.getId()==0)
            {
                changeCursor(cursor);
                if ((adapter.getGroupCount()!=0))
                    expandableListView.setSelection(adapter.getGroupCount()-1);
            }
            else
            try {
                setChildrenCursor(((CSQLiteOpenHelper.CSQLiteCursorLoader)cursorLoader)
                        .getData().getInt(GROUP_POS), cursor);
            } catch (NullPointerException e)  {
                e.printStackTrace();
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {
                changeCursor(null);
        }

        protected View newGroupView(Context context, Cursor cursor, boolean b, ViewGroup viewGroup) {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return infalInflater.inflate(R.layout.expand_list_group, null);
        }

        protected void bindGroupView(View view, Context context, Cursor cursor, boolean b) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndex("timemark")));
            ((TextView)view.findViewById(R.id.text1)).setText(
                    calendar.get(Calendar.DATE) + ":" +
                    Integer.toString(calendar.get(Calendar.MONTH)+1) + ":" +
                    calendar.get(Calendar.YEAR) + " " +
                    calendar.get(Calendar.HOUR_OF_DAY) + ":" +
                    calendar.get(Calendar.MINUTE));
        }

        protected View newChildView(Context context, Cursor cursor, boolean b, ViewGroup viewGroup) {
            LinearLayout view = new LinearLayout(context);
            view.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT,
                    ListView.LayoutParams.WRAP_CONTENT));
            view.setBackgroundColor(Color.BLACK);
            view.setOrientation(LinearLayout.VERTICAL);
            return view;
        }

        protected void bindChildView(View view, Context context, Cursor cursor, boolean b) {
            ((LinearLayout)view).removeAllViews();
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            CDistance distance = new CDistance(cursor);
            sum = 0;
            LinearLayout statisticsBlock;
            for (int i =0; i<distance.ends.size()/2;i++)
            {
                statisticsBlock = (LinearLayout) infalInflater.inflate(R.layout.statistics_block,null);
                fillStatisticsBlockView(statisticsBlock, new CShot[][]{distance.ends.get(i*2).toArray(new CShot[0]),
                        distance.ends.get(i*2 + 1).toArray(new CShot[0])});
                ((LinearLayout) view).addView(statisticsBlock);
            }
            if (distance.ends.size()%2!=0)
            {
                statisticsBlock = (LinearLayout) infalInflater.inflate(R.layout.statistics_block,null);
                fillStatisticsBlockView(statisticsBlock, new CShot[][]{distance.ends.lastElement().toArray(new CShot[0]),
                        null});
                ((LinearLayout) view).addView(statisticsBlock);
            }
        }
    }

}
