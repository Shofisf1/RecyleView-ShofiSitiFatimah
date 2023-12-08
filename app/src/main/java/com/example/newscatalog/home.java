  package com.example.newscatalog;

  import androidx.appcompat.app.AppCompatActivity;
  import android.os.Bundle;
  import android.view.View;
  import android.widget.Button;
  import android.content.Intent;

  public class home extends AppCompatActivity {
      private Button btnlogin,btnsignup;
      @Override
      protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_home);
          btnlogin = findViewById(R.id.login_button);
          btnsignup = findViewById(R.id.signup_button);

          btnlogin.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  Intent intent = new Intent( home.this,login.class);
                  startActivity(intent);
              }
          });
          btnsignup.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {

                  Intent intent = new Intent( home.this,signup.class);
                  startActivity(intent);
              }
          });
      }
  }
