package com.jane.mechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetActivity extends AppCompatActivity {
    private TextInputEditText editTextForget;
    private ImageView imageViewForget;
    private Button buttonReset;

    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget);
        editTextForget = findViewById(R.id.edittextforget);
        imageViewForget = findViewById(R.id.imageViewforget);
        buttonReset = findViewById(R.id.buttonreset);

        auth = FirebaseAuth.getInstance();
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextForget.getText().toString();
                if(!email.equals("")){
                    passwordReset(email);
                }
            }
        });
    }

    public void passwordReset (String email){
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ForgetActivity.this, "please check your email", Toast.LENGTH_SHORT);
                } else {
                    Toast.makeText(ForgetActivity.this, "there is a problem", Toast.LENGTH_SHORT);
                }
            }
        });
    }


}