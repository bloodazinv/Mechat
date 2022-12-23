package com.jane.mechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends AppCompatActivity {
    private CircleImageView imageviewcircle;
    private TextInputEditText editTextEmail, editTextPassword, editTextUserName;
    private Button buttonSignUp;
    private boolean imageControl = false;

    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference reference;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    Uri imageURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        imageviewcircle = findViewById(R.id.imageviewcircle);
        editTextEmail = findViewById(R.id.signupemail);
        editTextPassword = findViewById(R.id.signuppassword);
        editTextUserName = findViewById(R.id.edittextusername);
        buttonSignUp = findViewById(R.id.buttonsignup);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://mechat-4ee30-default-rtdb.europe-west1.firebasedatabase.app/");
        if(database == null) {
            System.out.println("failed connected database");
        } else {
            System.out.println("database: " + database);
        }
        reference = database.getReference();
        //firebase storage
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        imageviewcircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageChooser();
            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();
                String userName = editTextUserName.getText().toString();

                if(!email.equals("") && !password.equals("") && !userName.equals("")){
                    signup(email,password,userName);
                }
            }
        });

    }

    //user choose their profile picture
    public void imageChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK && data != null){
            imageURL = data.getData();
            Picasso.get().load(imageURL).into(imageviewcircle);
            imageControl = true;
        } else {
            imageControl = false;
        }
    }

    private void signup(String email, String password, String userName){
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    System.out.println("taskissuccessful");
                    reference.child("Users").child(auth.getUid()).child("userName").setValue(userName).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            System.out.println("database completed");
                        }
                    });//add new user into database
                    System.out.println("referencing");
                    if(imageControl){
                        UUID randomID = UUID.randomUUID();//create a unique id for each image
                        String imageName = "images/" + randomID + ".jpg";
                        System.out.println("imagecontrol");
                        storageReference.child(imageName).putFile(imageURL).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                System.out.println("success connect storage");
                                StorageReference myStorageRef = firebaseStorage.getReference(imageName);//represents the image saved in the database
                                myStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) { //uri of the saved image
                                        String filePath = uri.toString();
                                        reference.child("Users").child(auth.getUid()).child("image").setValue(filePath).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(SignUpActivity.this, "Write to database is successful.", Toast.LENGTH_SHORT);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(SignUpActivity.this, "Write to database is unsuccessful.", Toast.LENGTH_SHORT);
                                            }
                                        });
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                System.out.println("storage failed!");
                                Toast.makeText(SignUpActivity.this, "Write to database is unsuccessful.", Toast.LENGTH_SHORT);
                            }
                        });

                    } else {
                        reference.child("Users").child(auth.getUid()).child("image").setValue(null);
                        Toast.makeText(SignUpActivity.this, "Profile picture not found.", Toast.LENGTH_LONG);
                    }
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);

                    startActivity(intent);
                    finish();
                } else {
                    System.out.println("taskisNOTsuccessful");
                    Toast.makeText(SignUpActivity.this, "There is a problem.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}