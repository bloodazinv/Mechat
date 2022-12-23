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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private CircleImageView imageViewCircleProfile;
    private TextInputEditText editTextUserNameProfile;
    private Button buttonUpdate;
    String image;

    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseAuth auth;
    FirebaseUser user;

    Uri imageUri;
    boolean imageControl = false;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imageViewCircleProfile = findViewById(R.id.imageviewcircleProfile);
        editTextUserNameProfile = findViewById(R.id.edittextusernameProfile);
        buttonUpdate = findViewById(R.id.buttonUpdate);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        //firebase storage
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        getUserInfo();
        imageViewCircleProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageChooser();
            }
        });

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
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

    public void updateProfile(){
        String userName = editTextUserNameProfile.getText().toString();
        reference.child("Users").child(user.getUid()).child("userName").setValue(userName);

        if(imageControl){
            UUID randomID = UUID.randomUUID();//create a unique id for each image
            String imageName = "images/" + randomID + ".jpg";
            System.out.println("!!!!!!!!!!!!!!!!imagecontrol");
            storageReference.child(imageName).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    System.out.println("!!!!!!!!!!!!!!!!!!success connect storage");
                    StorageReference myStorageRef = firebaseStorage.getReference(imageName);//represents the image saved in the database
                    myStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) { //uri of the saved image
                            String filePath = uri.toString();
                            reference.child("Users").child(auth.getUid()).child("image").setValue(filePath).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(ProfileActivity.this, "Write to database is successful.", Toast.LENGTH_SHORT);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ProfileActivity.this, "Write to database is unsuccessful.", Toast.LENGTH_SHORT);
                                }
                            });
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    System.out.println("!!!!!!!!!!!!!!!!storage failed!");
                    Toast.makeText(ProfileActivity.this, "Write to database is unsuccessful.", Toast.LENGTH_SHORT);
                }
            });

        } else {
            reference.child("Users").child(auth.getUid()).child("image").setValue(image);
            Toast.makeText(ProfileActivity.this, "Profile picture not found.", Toast.LENGTH_LONG);
        }
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.putExtra("UserName", userName);//send username to the main activity
        startActivity(intent);
        finish();
    }

    public void getUserInfo(){
        reference.child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("username").getValue().toString();
                image = snapshot.child("image").getValue().toString();
                editTextUserNameProfile.setText(name);

                if(image.equals("null")){
                    imageViewCircleProfile.setImageResource(R.drawable.account);

                } else {
                    Picasso.get().load(image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(imageViewCircleProfile);
            imageControl = true;
        } else {
            imageControl = false;
        }
    }
}