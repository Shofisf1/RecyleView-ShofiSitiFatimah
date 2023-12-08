package com.example.newscatalog;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
public class NewsAdd extends AppCompatActivity {
    EditText title, desc;
    Button saveNews;
    FirebaseFirestore dbNews = FirebaseFirestore.getInstance();
    ProgressDialog progressDialog;
    String id = " ";
    ImageView gambar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_add);

        title = findViewById(R.id.title);
        desc = findViewById(R.id.desc);
        saveNews = findViewById(R.id.btnAdd);
        gambar = findViewById(R.id.imgNews);
        gambar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        progressDialog = new ProgressDialog(NewsAdd.this);
        progressDialog.setTitle("Sedang diproses...");

        saveNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (title.getText().length() > 0 && desc.getText().length() > 0){
                    upload(title.getText().toString(), desc.getText().toString());
                }else {
                    Toast.makeText(getApplicationContext(), "Semua data harus diisi", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Intent updateOption = getIntent();
        if (updateOption!=null){
            id = updateOption.getStringExtra("id");
            title.setText(updateOption.getStringExtra("title"));
            desc.setText(updateOption.getStringExtra("description"));
            Glide.with(getApplicationContext()).load(updateOption.getStringExtra("img")).
                    into(gambar);
        }
    }

    private void upload(String title, String description){
        progressDialog.show();
        gambar.setDrawingCacheEnabled(true);
        gambar.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) gambar.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference referenceStorage = storage.getReference("images")
                .child("IMG" + new Date().getTime() + ".jpg");
        UploadTask uploadTask = referenceStorage.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(getApplicationContext(), "Data gagal diupload", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        saveData(title, description, task.getResult().toString());
                    }
                });
                Toast.makeText(getApplicationContext(), "Data berhasil diupload",
                        Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
    }

    private void selectImage(){
        CharSequence[] optionAtion = {"Take photo", "Choose from library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(NewsAdd.this);
        builder.setTitle(R.string.app_name);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setItems(optionAtion,(dialogInterface, i) -> {
            if (optionAtion[i].equals("Take photo")){
                Intent take = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(take, 10);
            }else if (optionAtion[i].equals("Choose from library")){
                Intent pick = new Intent(Intent.ACTION_PICK);
                pick.setType("image/*");
                startActivityForResult(Intent.createChooser(pick, "Select Image"), 20);
            }else{
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final Uri path = data.getData();
        if (requestCode == 20 && resultCode == RESULT_OK && data!=null){
            Thread thread = new Thread(()->{
                try {
                    InputStream inputStream = getContentResolver().openInputStream(path);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    gambar.post(()->{gambar.setImageBitmap(bitmap);});
                }catch (IOException e){
                    e.printStackTrace();
                }
            });
            thread.start();
        }
        if (requestCode == 10 && resultCode == RESULT_OK){
            final Bundle extras = data.getExtras();
            Thread thread = new Thread(()->{
                Bitmap bitmap = (Bitmap) extras.get("data");
                gambar.post(()->{gambar.setImageBitmap(bitmap);});
            });
            thread.start();
        }
    }

    private void saveData(String title, String description, String Gambar){
        Map<String, Object> mapNews = new HashMap<>();
        mapNews.put("title", title);
        mapNews.put("desc", description);
        mapNews.put("img", Gambar);

        progressDialog.show();
        if (id!=null) {
            //edit data
            dbNews.collection("news").document(id)
                    .set(mapNews)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("data", "DocumentSnapshot successfully written!");
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("data", "Error writing document", e);
                            finish();
                        }
                    });
        }else {
            //save data
            //add new document with a generate ID
            dbNews.collection("news").add(mapNews).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("data", "DocumentSnapshot added with ID: " + documentReference.getId());
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("data", "Error adding document", e);
                        }
                    });
        }
        progressDialog.dismiss();
    }
}