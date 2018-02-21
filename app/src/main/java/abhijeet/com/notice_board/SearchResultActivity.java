package abhijeet.com.notice_board;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class SearchResultActivity extends AppCompatActivity {

    private static final String TAG="Search Result";
    String dept;
    public static ArrayList<NoticeContents> searchResultList;
    private DatabaseReference mDatabase;
    private RecyclerView mRecyclerView;
    public static ProgressDialog progressDialog;
    public static CardViewAdapter mAdapter;

    private String filepath[] , mime[];
    FirebaseStorage storage;
    StorageReference storageRef;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice_list);
        getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.grey));
        progressDialog = new ProgressDialog(SearchResultActivity.this);
        progressDialog.show();
        progressDialog.setMessage("Loading");

        filepath = new String[1];
        mime = new String[1];

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://notice-board-babe0.appspot.com");

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        Intent intent = getIntent();
        dept = intent.getStringExtra("dept");
        handleIntent(getIntent());

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        Toast.makeText(getApplicationContext(), "Position is " + position, Toast.LENGTH_SHORT).show();

                        NoticeContents nc = searchResultList.get(position);
                        String timestamp = "" + nc.timestamp;

                        mDatabase.child("admin").child(dept).child(timestamp).addListenerForSingleValueEvent(new ValueEventListener() {

                            // Gets snapshot of the database
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                // Gets required department preference for current user
                                filepath[0] = dataSnapshot.child("url").getValue(String.class);

                                Log.d("filepath", filepath[0]);

                                mime[0] = CardNoticeList.getMimeType(filepath[0]);

                                Log.d("mime", mime[0]);


                                if (!mime[0].contains("image")) {
                                    StorageReference httpsReference = storage.getReferenceFromUrl(filepath[0]);

                                    final long ONE_MEGABYTE = 4096 * 4096;

                                    httpsReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                        @Override
                                        public void onSuccess(byte[] bytes) {
                                            File path = Environment.getExternalStoragePublicDirectory(
                                                    Environment.DIRECTORY_PICTURES);
                                            File file1 = new File(path, "url");

                                            try {
                                                Log.d("Directory is", path.mkdirs() + "");
                                                OutputStream os = new FileOutputStream(file1);
                                                os.write(bytes);
                                                os.close();
                                                Log.d("directory ", file1.getAbsolutePath());
                                                Log.d("type ", mime[0]);
                                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                                intent.setDataAndType(Uri.fromFile(file1), mime[0]);
                                                startActivity(intent);

                                            } catch (IOException e) {
                                                // Unable to create file, likely because external storage is
                                                // not currently mounted.
                                                Log.w("ExternalStorage", "Error writing " + file1, e);
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            // Handle any errors
                                        }
                                    });
                                } else {

                                    Intent intent = new Intent();
                                    intent.setClass(SearchResultActivity.this, ViewNotice.class);
                                    intent.putExtra("url", filepath[0]);
                                    startActivity(intent);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d("bum", "on cancelled error");
                            }
                        });
                        // do whatever
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );
    }

    public void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doSearch(query);
        }
    }

    private void doSearch(final String queryStr) {

        final String search = queryStr.toLowerCase();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Query query = mDatabase.child("admin").child(dept);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                searchResultList = new ArrayList<NoticeContents>();

                for (DataSnapshot postDatasnapshot : dataSnapshot.getChildren()) {
                    String title = postDatasnapshot.child("title").getValue(String.class);
                    String type = postDatasnapshot.child("type").getValue(String.class);
                    if(title.toLowerCase().contains(search) || type.toLowerCase().contains(search)) {
                        long timestamp = Long.parseLong(postDatasnapshot.getKey());
                        String url = postDatasnapshot.child("url").getValue(String.class);
                        String sem = "";
                        for (DataSnapshot ds : postDatasnapshot.child("semester").getChildren()) {
                            if (ds.getValue(Boolean.class)) {
                                sem = sem + ds.getKey() + ", ";
                            }
                        }
                        NoticeContents newNotice = new NoticeContents(title, timestamp, url, sem, type);
                        searchResultList.add(newNotice);
                    }
                }

                if(searchResultList.isEmpty())
                {
                    Toast.makeText(SearchResultActivity.this, "Sorry, not found", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setClass(SearchResultActivity.this, CardNoticeList.class);
                    intent.putExtra("dept", dept);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                Collections.reverse(searchResultList);
                // Create the adapter to convert the array to views
                mAdapter = new CardViewAdapter(getApplicationContext(),searchResultList);

                mAdapter.notifyDataSetChanged();

                mRecyclerView.setAdapter(mAdapter);

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG,databaseError.toString());
            }

        });
    }
}


