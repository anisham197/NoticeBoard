// Creates a custom adapter for the recycler view in deptPreferences.xml

package abhijeet.com.notice_board;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class AdapterSetPreferences extends RecyclerView.Adapter<AdapterSetPreferences.MyViewHolder>{

      // Contains a list of checkboxes with dept names
    private List<ListContents> data;

    private DatabaseReference mDatabase;
    private FirebaseUser user;

    // Initializes the list when adapter is set
    public AdapterSetPreferences(List<ListContents> data)
    {
        this.data = data;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        CheckBox ch;

        // Assigns a tag to the textview and checkbox item by obtaining the ID
        public MyViewHolder(View itemView)
        {
            super(itemView);
            name = (TextView)itemView.findViewById(R.id.todoName);
            ch = (CheckBox)itemView.findViewById(R.id.isComplete);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // Creates a layout with name and checkbox and inflates it
        View itemrow = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_items_dept_preferences, parent, false);
        return new MyViewHolder(itemrow);
    }

    // Sets the name and checkbox for each item along with a listener for the checkbox
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        ListContents temp = data.get(position);
        holder.name.setText(temp.name);
        holder.ch.setChecked(temp.isTicked);


        holder.ch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                data.get(position).isTicked = !data.get(position).isTicked;

                //TODO: add extra functionality
            }
        });

    }

    // Contains the number of items in the adapter
    @Override
    public int getItemCount()
    {
        return data.size();
    }
}