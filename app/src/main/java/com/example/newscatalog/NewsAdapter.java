package com.example.newscatalog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsHolder>{
    private List<NewsItem> newsItems;
    Dialog dialog;
    Context context;
    public  interface Dialog{
        void onClick(int pos);
    }
    public Dialog getDialog() {
        return dialog;
    }
    public void setDialog(Dialog dialog){
        this.dialog = dialog;
    }
    public NewsAdapter(Context context, List<NewsItem> newsItems){
        this.newsItems = newsItems;
    }
    @NonNull
    @Override
    public NewsAdapter.NewsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext().getApplicationContext()).inflate(R.layout.news_item, null);
        NewsHolder vh = new NewsHolder(v);
        return vh;
    }
    @Override
    public void onBindViewHolder(@NonNull NewsAdapter.NewsHolder holder, int position){
        NewsItem item = newsItems.get(position);
        holder.judul.setText(item.getJudul());
        holder.desc.setText(item.getDesc());
        Glide.with(holder.imageView.getContext()).load(item.getImage()).into(holder.imageView);
    }
    @Override
    public int getItemCount(){
        return newsItems.size();
    }
    public class NewsHolder extends RecyclerView.ViewHolder{
        public ImageView imageView;
        public TextView id, judul, short_desc, desc;
        public NewsHolder(@NonNull View itemView){
            super(itemView);
            judul = itemView.findViewById(R.id.title);
            desc = itemView.findViewById(R.id.desc);
            imageView = itemView.findViewById(R.id.image);
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    dialog.onClick(getLayoutPosition());
                }
            });
        }
    }
}
