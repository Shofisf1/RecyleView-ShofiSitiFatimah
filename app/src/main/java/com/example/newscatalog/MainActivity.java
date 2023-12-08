package com.example.newscatalog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerViewNews;
    FloatingActionButton floatingNews;
    FirebaseFirestore dbNews = FirebaseFirestore.getInstance();
    List<NewsItem> newsItems = new ArrayList<>();
    NewsAdapter newsAdapter;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerViewNews = findViewById(R.id.rcvNews);
        floatingNews = findViewById(R.id.floatAddNews);
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Proses menampilkan data...");

        newsAdapter = new NewsAdapter(getApplicationContext(), newsItems);
        newsAdapter.setDialog(new NewsAdapter.Dialog() {
            @Override
            public void onClick(int pos) {
                CharSequence[] optionAtion = {"Edit", "Hapus"};
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setItems(optionAtion, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0:
                                Intent update = new Intent(MainActivity.this, NewsAdd.class);
                                update.putExtra("id", newsItems.get(pos).getId());
                                update.putExtra("title", newsItems.get(pos).getJudul());
                                update.putExtra("descrption", newsItems.get(pos).getDesc());
                                update.putExtra("img", newsItems.get(pos).getImage());
                                startActivity(update);
                                break;
                            case 1: deleteData(newsItems.get(pos).getId());
                                break;
                        }
                    }
                });
                dialog.show();
            }
        });

        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false);
        RecyclerView.ItemDecoration decoration = new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL);
        recyclerViewNews.setLayoutManager(layoutManager);
        recyclerViewNews.addItemDecoration(decoration);
        recyclerViewNews.setAdapter(newsAdapter);

        floatingNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toAddPage = new Intent(MainActivity.this, NewsAdd.class);
                startActivity(toAddPage);
            }
        });
    }

    protected void onStart(){
        super.onStart();
        getData();
    }

    private void getData() {
        progressDialog.show();
        dbNews.collection("news").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    newsItems.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        NewsItem item = new NewsItem(document.getString("title"),
                                document.getString("desc"), document.getString("img"));
                        item.setId(document.getId());
                        newsItems.add(item);
                        Log.d("data", document.getId() + " => " + document.getData());
                    }
                    newsAdapter.notifyDataSetChanged();
                } else {
                    Log.w("data", "Error getting documents.", task.getException());
                }
                progressDialog.dismiss();
            }
        });
    }

    private void deleteData(String id){
        progressDialog.show();
        dbNews.collection("news").document(id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                getData();
                progressDialog.dismiss();
                Log.d("data", "DocumentSnapshot successfully deleted!");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        getData();
                        progressDialog.dismiss();
                        Log.w("data", "Error deleting document", e);
                    }
                });
    }

}