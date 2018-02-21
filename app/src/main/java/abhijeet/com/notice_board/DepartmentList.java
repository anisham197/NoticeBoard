package abhijeet.com.notice_board;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class DepartmentList extends AppCompatActivity {

    private FirebaseAuth mAuth;


    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.department_list);

        // The adapter is initialised with department names
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.textview_department_list, MainActivity.Departments);

        // Adapter mapped to listView
        final ListView listView = (ListView) findViewById(R.id.department_list);
        listView.setAdapter(adapter);
    }


    // Function that executes when department name is clicked
    public void textViewClick(View v){

        TextView tv = (TextView)v;
        String msg = tv.getText().toString();
        Intent intent2 = new Intent();
        intent2.setClass(DepartmentList.this, CardNoticeList.class);
        intent2.putExtra("dept", msg);
        startActivity(intent2);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id){

            case R.id.action_preferences:
                // Shifts to initial change preference activity if form is validated
                Intent intent = new Intent();
                intent.setClass(DepartmentList.this, SetPreferences.class);
                startActivity(intent);
                break;

            case R.id.action_appinfo:

                Intent intent3 = new Intent();
                intent3.setClass(DepartmentList.this, Settings.class);
                startActivity(intent3);
                break;


            case R.id.out:
                mAuth = FirebaseAuth.getInstance();

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
               // FirebaseUser user = mAuth.getCurrentUser();
                if ( user != null){
                    // Signs out user
                    mAuth.signOut();

                    pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                    editor = pref.edit();
                    editor.putString("uid", "");
                    editor.apply();

                    stopService(new Intent(this, NotificationService.class));

                    Log.d("Anisha", "Service Stopped");

                    // Shifts to main activity and clears stack trace
                    Intent intent2 = new Intent();
                    intent2.setClass(DepartmentList.this, MainActivity.class);
                    intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent2);
                }
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    // Disables back button
    @Override
    public void onBackPressed() {}
}