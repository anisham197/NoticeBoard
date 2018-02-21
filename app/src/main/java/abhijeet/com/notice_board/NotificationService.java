package abhijeet.com.notice_board;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationService extends Service {

    private static int ID = 0;

    private String uid;

    private DatabaseReference mDatabase;
    private FirebaseUser user;

    SharedPreferences pref;

    long timestamp; //= System.currentTimeMillis();

    public static String[] Departments = {"Architecture", "Biotechnology", "Chemical", "Civil", "Computer Science",
            "Electrical and Electronics", "Electronics and Communication", "Electronics and Instrumentation",
            "Industrial Engineering and Management", "Information Science", "Mechanical",
            "Medical Electronics", "Telecommunication"};

    final int size = Departments.length;
    // Stores dept preferences
    public boolean[] deptPreferences;

    @Override
    public void onCreate() {
        // Code to execute when the service is first created
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

        Toast.makeText(this, "service stopping", Toast.LENGTH_SHORT).show();
        Log.d("Anisha", "Service stopped inside NS");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {

        timestamp = System.currentTimeMillis();

        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        Log.d("Anisha", "NS size " + size);

        deptPreferences = new boolean[size];

        // Code to execute when the service is first created
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        uid = pref.getString("uid", null);

        Log.d("Anisha", "NS UID " + uid);

        getPreference();

        setDatabaseListeners();

        return 0;
    }

    public void sendNotification(String dept, String title)
    {
        NotificationCompat.Builder notify = new NotificationCompat.Builder(this);
        notify.setAutoCancel(true);

        notify.setSmallIcon(R.drawable.icon);
        notify.setTicker("New notice from " + dept);
        notify.setContentTitle(title);
        notify.setContentText(dept);
        notify.setWhen(System.currentTimeMillis());

        Intent intent =  new Intent(this, CardNoticeList.class);
        intent.putExtra("dept", dept);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notify.setContentIntent(pendingIntent);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(ID, notify.build());
        ID++;
        timestamp = System.currentTimeMillis();
    }


    public void getPreference()
    {
        //Toast.makeText(this, mRef.toString(), Toast.LENGTH_SHORT).show();
        mDatabase.child("ID's").child(uid).addValueEventListener(new ValueEventListener() {
            // Gets snapshot of the database
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (int i = 0; i < size; i++) {
                    deptPreferences[i] = dataSnapshot.child(Departments[i]).getValue(Boolean.class);
                     Log.d("Anisha", "" + deptPreferences[i]);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // System.out.println("The read failed: " + databaseError.getMessage());
            }
        });
    }
//did the service stop? wait let me check
    public void setDatabaseListeners()
    {
        for(int i = 0; i < size; i++) {
            mDatabase.child("admin").child(Departments[i]).addChildEventListener(new ChildEventListener() {

                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                    String dept = dataSnapshot.getRef().getParent().getKey();
                    Log.d("Anisha", dept);

                    long noticeTimestamp = Long.parseLong(dataSnapshot.getKey());
                    Log.d("Anisha", "notice timestamp "  + DateUtils.formatDateTime(getBaseContext(),noticeTimestamp,DateUtils.FORMAT_SHOW_TIME));

                    for (int j = 0; j < size; j++) {


                        Log.d("Anisha", "Dept[j]:" + Departments[j] );
                       // boolean value = dataSnapshot.child("dept").child(Departments[j]).getValue(Boolean.class);


                        if (dept.equals(Departments[j]) && deptPreferences[j] && noticeTimestamp > timestamp && !uid.equals(""))
                        {
                            sendNotification(dept, dataSnapshot.child("title").getValue(String.class));
                            break;

                        }
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}


