// Class that allows user to select preferences for notifications
package abhijeet.com.notice_board;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SetPreferences extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseUser user;

    private RecyclerView prefContainer;

    final boolean[] value = {false};

    // Contains a list of departments with checkboxes
    public static List<ListContents> prefList;

    public static AdapterSetPreferences adapter;

    public static ProgressDialog progressDialog;

    // List of Departments


    // Size of list
    final int size = MainActivity.Departments.length;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dept_preferences);
        progressDialog = new ProgressDialog(SetPreferences.this);

        progressDialog.show();
        progressDialog.setMessage("Loading");

        prefList = new ArrayList<>();

        prefContainer = (RecyclerView) findViewById(R.id.deptPref);

        prefContainer.setLayoutManager(new LinearLayoutManager(this));


        // Initializes adapter with prefList
        adapter = new AdapterSetPreferences(prefList);


        // Initializes the prefList from database, notifies adapter
        for(int i = 0; i < size; i++)
        {
           GetPreferences(i);
        }

          // Mapping the recycler view with the adapter
        prefContainer.setAdapter(adapter);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:

                // Sets user's preferences to database
                SetPreferences();

                Intent intent = new Intent();
                intent.setClass(SetPreferences.this, DepartmentList.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    // Disables back button
    @Override
    public void onBackPressed() {}

    // Function to store preferences along with UID's
    public void SetPreferences()
    {
        ListContents LC;

        // Gets reference of database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Gets current user and UID
        user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        // Initializes hashmap to store preferences in database
        HashMap<String, Boolean> hm = new HashMap<>();

        // Adding preferences to hashmap
        for ( int i = 0; i < size; i++)
        {
            LC = prefList.get(i);      // Gets ListContents object from preflist
            hm.put(MainActivity.Departments[i], LC.isTicked);
        }

        // Uploads hashmap to database
        mDatabase.child("ID's").child(uid).setValue(hm);
    }


    // Function to retrieve preferences from database
    public void GetPreferences(final int position)
    {
        // Gets reference of database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Gets current user and UID
        user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();

        // Adds listener to monitor changes to database, called initially and when any change occurs

        mDatabase.child("ID's").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            // Gets snapshot of the database
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Gets required department preference for current user
                value[0] = dataSnapshot.child(MainActivity.Departments[position] ).getValue(Boolean.class);
                String key = dataSnapshot.child(MainActivity.Departments[position] ).getKey();
                // Adds department along with boolean value to preflist
                prefList.add(new ListContents(MainActivity.Departments[position], value[0]));
                adapter.notifyDataSetChanged();     // Adapter is notified of changes

                if ( key.equals(MainActivity.Departments[size - 1]));
                   progressDialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //System.out.println("The read failed: " + databaseError.getMessage());
            }
        });
    }
}