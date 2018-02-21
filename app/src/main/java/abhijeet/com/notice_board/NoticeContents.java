// Class to define title and timestamp for listview

package abhijeet.com.notice_board;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NoticeContents {
    public String title;
    public String date;
    public long timestamp;
    public String url;
    public String semester;
    public String type;

    public NoticeContents(String title, long timestamp, String url, String semester, String type) {

        // Initialises title of notice
        this.title = title;

        this.timestamp = timestamp;

        this.semester = semester;

        this.type = type;

        this.url = url;
        // Converts timestamp from long to String
        Date currentDate = new Date(timestamp);
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");//:HH:mm:ss
        this.date = df.format(currentDate);
    }
}