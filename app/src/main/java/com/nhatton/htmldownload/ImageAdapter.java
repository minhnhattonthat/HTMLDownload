package com.nhatton.htmldownload;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by nhatton on 8/26/17.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageHolder> {

    DownloadImageTask.DownloadListener listener;

    private ArrayList<String> items;
    private Context context;

    ImageAdapter(ArrayList<String> items, Context context) {
        ImageLoader.getInstance().setUrlList(items);

        //Start download all images in background
        final Runnable task = new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                ImageLoader.startDownloadAll();
            }
        };
        Thread thread = new Thread(task);
        thread.start();

        this.items = items;
        this.context = context;
        this.listener = new AdapterDownloadListener(this);
    }

    private static class AdapterDownloadListener implements DownloadImageTask.DownloadListener {
        private ImageAdapter imageAdapter;

        AdapterDownloadListener(ImageAdapter imageAdapter) {
            this.imageAdapter = imageAdapter;
        }

        @Override
        public void onDownloadFinished(int position) {
            imageAdapter.noticeFinished();
        }
    }

    private void noticeFinished() {

    }

    @Override
    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.row_image, parent, false);
        return new ImageHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ImageHolder holder, int position) {
        holder.setIsRecyclable(false);
        holder.positionView.setText(String.valueOf(position));

//        ImageLoader.startDownload(holder.imageView, position);

        //Try to set image until get bitmap
        final Handler handler = new Handler();
        final Runnable task = new Runnable() {
            @Override
            public void run() {
                if (ImageLoader.getInstance().getBitmaps().get(holder.getAdapterPosition()) != null) {
                    holder.imageView.setImageBitmap(ImageLoader.getInstance().getBitmaps().get(holder.getAdapterPosition()));
                } else {
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(task);

//        new DownloadImageTask(holder.imageView, position, listener).execute(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ImageHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView positionView;

        ImageHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            positionView = itemView.findViewById(R.id.position_number);

        }
    }

}
