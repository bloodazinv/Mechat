package com.jane.mechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edittextemail, edittextpassword;
    private Button signinbutton, signupbutton;
    private TextView textviewforget;

    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onStart(){
        super.onStart();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){ //if user has logged in, they can directly come to the main activity page
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        edittextemail = findViewById(R.id.signupemail);
        edittextpassword = findViewById(R.id.signuppassword);
        signinbutton = findViewById(R.id.signinbutton);
        signupbutton = findViewById(R.id.signupbutton);
        textviewforget = findViewById(R.id.textviewforget);

        auth = FirebaseAuth.getInstance();

        signinbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edittextemail.getText().toString();
                String password = edittextpassword.getText().toString();
                if(!email.equals("") && !password.equals("")){
                    signin(email, password);
                } else{
                    Toast.makeText(LoginActivity.this, "Please enter your Email and password.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        signupbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class );//open related activity
                startActivity(intent);
            }
        });
        textviewforget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ForgetActivity.class );
                startActivity(intent);
            }
        });


    }


    public void signin(String email, String password){
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    Toast.makeText(LoginActivity.this, "Successfully Sign In", Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginActivity.this, "Login process has encountered failure, please try again later", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}