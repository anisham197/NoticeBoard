package abhijeet.com.notice_board;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.ArrayList;

public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.ViewHolder> {

    private ArrayList<NoticeContents> listOfNotices;
    private final Context context;

    public CardViewAdapter(Context context,ArrayList<NoticeContents> myDataset) {
        this.listOfNotices = myDataset;
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder  {

        TextView title;
        TextView date;
        TextView semester;
        TextView type;
        ImageView notice_photo;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            date = (TextView) itemView.findViewById(R.id.date);
            semester = (TextView) itemView.findViewById(R.id.semester);
            type = (TextView) itemView.findViewById(R.id.type);
            notice_photo = (ImageView) itemView.findViewById(R.id.notice_photo);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_notice_list, parent, false);
        //view.setOnClickListener(mOnClickListener);
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.title.setText( listOfNotices.get(position).title);
        holder.date.setText(listOfNotices.get(position).date);
        holder.type.setText("Category: " + listOfNotices.get(position).type);
        String sem = listOfNotices.get(position).semester;

        if ( !sem.isEmpty() )
            sem = sem.substring(0,sem.length()-2);
        holder.semester.setText("Semester: " + sem );
        String mime = CardNoticeList.getMimeType(listOfNotices.get(position).url);

        if ( mime.contains("image")  ) {

            Picasso.with(context).load(listOfNotices.get(position).url).fit()
                   .placeholder(R.drawable.loading)
                   //.error(R.drawable.no_image)
                   .into(holder.notice_photo, new Callback() {
                       @Override
                       public void onSuccess() {
                           Log.d("image msg", " pic set ");
                       }

                       @Override
                       public void onError() {

                           Log.d("image msg", " pic error ");
                       }
                   });
        }

        if ( mime.equals(CardNoticeList.pdf)  )
            holder.notice_photo.setImageResource(R.drawable.pdf);
        //TODO Set listener
        if (  mime.equals(CardNoticeList.docx) ||  mime.equals(CardNoticeList.doc)   )
            holder.notice_photo.setImageResource(R.drawable.doc);
    }

    @Override
    public int getItemCount() {
        return listOfNotices.size();
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

        }
    }

}
