// Initialises the adapter which displays list of notices according to departments

package abhijeet.com.notice_board;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class AdapterNoticeList extends ArrayAdapter<NoticeContents> {

    // Constructor to initialise adapter with array of objects containing title and timestamp
    public AdapterNoticeList(Context context, ArrayList<NoticeContents> list) {
        super(context, 0, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        NoticeContents list = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.textview_notice_list, parent, false);
        }

        // Lookup view for data population
        TextView tvTitle = (TextView) convertView.findViewById(R.id.title);
        TextView tvDate = (TextView) convertView.findViewById(R.id.date);

        // Populate the data into the template view using the data object
        tvTitle.setText(list.title);
        tvDate.setText(list.date);

        // Return the completed view to render on screen
        return convertView;
    }
}