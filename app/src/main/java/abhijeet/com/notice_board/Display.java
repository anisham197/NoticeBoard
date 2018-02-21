package abhijeet.com.notice_board;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by anisha on 20/7/16.
 */

public class Display extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int flag = getIntent().getExtras().getInt("Option");

        switch (flag){

            case 1:
                setContentView(R.layout.about_us);
                break;

            case 3:
                setContentView(R.layout.terms);
                break;
            case 4:
                setContentView(R.layout.source_libraries);
                break;

        }

    }
}