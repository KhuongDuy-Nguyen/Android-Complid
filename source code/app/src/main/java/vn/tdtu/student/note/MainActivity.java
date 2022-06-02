package vn.tdtu.student.note;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import vn.tdtu.student.note.Label.Label;
import vn.tdtu.student.note.Label.LabelActivity;
import vn.tdtu.student.note.Login.LoginActivity;
import vn.tdtu.student.note.Note.Note;
import vn.tdtu.student.note.Note.NoteActivity;
import vn.tdtu.student.note.Trash.Trash;
import vn.tdtu.student.note.Trash.TrashActivity;
import vn.tdtu.student.note.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private  ArrayList<Note> notes;
    private  ArrayList<Label> labels;
    private  ArrayList<Trash> trashes;

    int noteSize, labelSize, trashSize;
    String email, username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initObject();
        initView();

        binding.category.setOnClickListener(view1 -> {
            Intent intent = new Intent(this, NoteActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        binding.labels.setOnClickListener(view2 -> {
            Intent intent = new Intent(this, LabelActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        binding.trash.setOnClickListener(view3 -> {
            Intent intent = new Intent(this, TrashActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        loadData();
        Handler handler = new Handler();
        handler.postDelayed(this::updateUI, 1000);

    }

    private void initView() {
        mAuth = FirebaseAuth.getInstance();

        email = user.getEmail();
        username = user.getEmail().replaceAll("@.*","").replaceAll("[^a-zA-Z]+", " ").trim();;

        binding.userName.setText(username);
        binding.userEmail.setText(email);

    }

    private void initObject(){
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        user = FirebaseAuth.getInstance().getCurrentUser();
    }


    @Override
    protected void onResume() {
        super.onResume();

        loadData();
        Handler handler = new Handler();
        handler.postDelayed(this::updateUI, 1000);
    }


    private void updateUI() {
        noteSize = notes.size();
        labelSize = labels.size();
        trashSize = trashes.size();
        binding.numberLabels.setText(String.valueOf(labelSize));
        binding.numberTrash.setText(String.valueOf(trashSize));
        binding.numberCategory.setText(String.valueOf(noteSize));
    }


    private void loadData() {

        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference().child(user.getUid());

        notes = new ArrayList<>();
        labels = new ArrayList<>();
        trashes = new ArrayList<>();

        mDatabase.child("notes").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Note note = snapshot.getValue(Note.class);
                notes.add(note);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mDatabase.child("labels").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Label label = snapshot.getValue(Label.class);
                labels.add(label);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mDatabase.child("trashes").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Trash trash = snapshot.getValue(Trash.class);
                trashes.add(trash);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.logout){
            DialogLogOut();
        }
        return true;
    }

    private void DialogLogOut(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setNegativeButton("Cancel", null);
        builder.setNeutralButton("OKE", (dialogInterface, i) -> {
            mAuth.signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
        builder.create().show();
    }
}