package abhijeet.com.notice_board;

/**
 * Created by anisha on 12/7/16.
 */

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;

import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class CardNoticeList extends AppCompatActivity implements SearchView.OnQueryTextListener{

    private String dept;
    private RecyclerView mRecyclerView;
    private DatabaseReference mDatabase;

    public static CardViewAdapter mAdapter;
    private ArrayList<NoticeContents> listOfNotices;

    private String filepath[] , mime[];
    FirebaseStorage storage;
    StorageReference storageRef;
    public static String pdf = "application/pdf";
    public static String doc = "application/msword";
    public static String docx = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    public static ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice_list);
        getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.grey));

        progressDialog = new ProgressDialog(CardNoticeList.this);

        progressDialog.show();
        progressDialog.setMessage("Loading");


        dept = getIntent().getExtras().getString("dept");

        filepath = new String[1];
        mime = new String[1];

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://notice-board-babe0.appspot.com");

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        RetrieveData();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                      //  Toast.makeText(getApplicationContext(), "Position is " + position, Toast.LENGTH_SHORT).show();

                        NoticeContents nc = listOfNotices.get(position);
                        String timestamp = "" + nc.timestamp;

                        // Toast.makeText(getApplicationContext(), "Timestamp " + timestamp, Toast.LENGTH_SHORT).show();

                        mDatabase.child("admin").child(dept).child(timestamp).addListenerForSingleValueEvent(new ValueEventListener() {

                            // Gets snapshot of the database
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                // Gets required department preference for current user
                                filepath[0] = dataSnapshot.child("url").getValue(String.class);

                                Log.d("filepath", filepath[0]);

                                mime[0] = getMimeType(filepath[0]);

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
                                    intent.setClass(CardNoticeList.this, ViewNotice.class);
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

    private void RetrieveData() {

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Gets list of all notices under given department
        mDatabase.child("admin").child(dept).addValueEventListener(new ValueEventListener() {
            // Gets snapshot of the database
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Contains array list of objects with title and timestamp
                listOfNotices = new ArrayList<NoticeContents>();

                for (DataSnapshot postDatasnapshot : dataSnapshot.getChildren()) {
                    // Retrieves title and timestamp for each image/pdf and stores it in array
                    String title = postDatasnapshot.child("title").getValue(String.class);
                    long timestamp = Long.parseLong(postDatasnapshot.getKey());
                    String url = postDatasnapshot.child("url").getValue(String.class);
                    String type = postDatasnapshot.child("type").getValue(String.class);
                    String sem = "";
                    for ( DataSnapshot ds : postDatasnapshot.child("semester").getChildren()){
                        if ( ds.getValue() == true){
                            sem = sem + ds.getKey() + ", ";
                        }
                    }
                   // String type = postDatasnapshot.child("type").getValue(String.class);
                    NoticeContents newNotice = new NoticeContents(title, timestamp, url, sem, type);
                    listOfNotices.add(newNotice);
                }

                Collections.reverse(listOfNotices);
                // Create the adapter to convert the array to views
                mAdapter = new CardViewAdapter(getApplicationContext(),listOfNotices);

                mAdapter.notifyDataSetChanged();

                mRecyclerView.setAdapter(mAdapter);

                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //System.out.println("The read failed: " + databaseError.getMessage());
            }
        });
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);

        if (extension != null) {

            Log.d("bum", extension);

            extension = extension.toLowerCase();

            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

            if (type != null )
                Log.d("bum", type );
        }
        return type;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_menu, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        ComponentName cn = new ComponentName(this, SearchResultActivity.class);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(cn));

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // UserDetails changed the text
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // UserDetails pressed the search button
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //noinspection SimplifiableIfStatement


        int id = item.getItemId();
        if(id == R.id.search)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void startActivity(Intent intent) {

        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
            intent.putExtra("dept", dept);
        }
        super.startActivity(intent);
    }

}

