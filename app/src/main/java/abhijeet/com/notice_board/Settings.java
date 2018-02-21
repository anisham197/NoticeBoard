package abhijeet.com.notice_board;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

/**
 * Created by anisha on 20/7/16.
 */

public class Settings extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

    }


    public void textViewClick(View v){

        int id = v.getId();
        Intent intent = new Intent();

        switch( id ){

            case R.id.about:
                intent.putExtra("Option", 1);
                break;

           /* case R.id.faq:
                intent.putExtra("Option", 2);
                break;*/

            case R.id.terms:
                intent.putExtra("Option", 3);
                break;

            case R.id.libraries:
                intent.putExtra("Option", 4);
                break;
        }

        intent.setClass(Settings.this, Display.class);
        startActivity(intent);


    }

}
