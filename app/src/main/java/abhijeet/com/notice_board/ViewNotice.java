// Displays an image or pdf of notice

package abhijeet.com.notice_board;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

public class ViewNotice extends AppCompatActivity {

    // Contains url of image/pdf
    String[] filepath, mime;

    String timestamp;
    String dept;
    FirebaseStorage storage;
    StorageReference storageRef;
    Bitmap store;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_notice);

        filepath = new String[1];
        filepath[0] = getIntent().getExtras().getString("url");


       new DownloadImageTask((ImageView) findViewById(R.id.imageView)).execute(filepath[0]);

    }

  private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap image = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
               image = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.d("Error", e.getMessage());
                e.printStackTrace();
            }
            return image;
        }

        protected void onPostExecute(Bitmap result) {

            bmImage.setImageBitmap(result);
            store = result;
        }
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.store_image, menu);
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

            case R.id.action_download:

                OutputStream output;
                // Find the SD Card path
                File filepath = Environment.getExternalStorageDirectory();

                // Create a new folder in SD Card
                File dir = new File(filepath.getAbsolutePath() + "/Notices/");
                dir.mkdirs();


                // Create a name for the saved image
                File file = new File(dir, System.currentTimeMillis() + ".jpg");

                // Show a toast message on successful save
                //Toast.makeText(MainActivity.this, "Image Saved to SD Card",
                        //Toast.LENGTH_SHORT).show();
                try {

                    output = new FileOutputStream(file);

                    // Compress into png format image from 0% - 100%
                    store.compress(Bitmap.CompressFormat.JPEG, 100, output);
                    output.flush();
                    output.close();
                    Toast.makeText(ViewNotice.this, "Image Saved to SD Card", Toast.LENGTH_SHORT).show();
                }

                catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                break;

        }

        return super.onOptionsItemSelected(item);
    }
}
