package vn.tdtu.student.note.Note;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import vn.tdtu.student.note.Label.Label;
import vn.tdtu.student.note.Label.LabelAdapter;
import vn.tdtu.student.note.MainActivity;
import vn.tdtu.student.note.R;
import vn.tdtu.student.note.databinding.ActivityListLabelBinding;
import vn.tdtu.student.note.databinding.ActivityListNoteBinding;

public class NoteActivity extends AppCompatActivity{

    private ActivityListNoteBinding binding;
    private DatabaseReference mDatabase;

    private final ArrayList<Note> notes = new ArrayList<>();
    private final ArrayList<Label> labels = new ArrayList<>();
    private final ArrayList<Note> original =  new ArrayList<>();

    private NoteAdapter adapter;
    private TagAdapter tagAdapter;
    private String username = "";
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        initObject();
        updateAdapterChange();

        binding.btnAddNote.setOnClickListener(view1 -> {
            Intent intentN = new Intent(this, AddNoteActivity.class);
            intentN.putExtra("username", username);
            startActivity(intentN);
        });

        binding.allList.setOnClickListener(view -> showListNoteByLabel("note"));

        binding.changeView.setOnCheckedChangeListener((compoundButton, isChange) -> {
            RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
            if(isChange){
                manager = new GridLayoutManager(this, 2);
            }else{
                manager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
            }
            binding.recyclerViewNote.setLayoutManager(manager);
        });

    }

    private void initView() {
        //binding
        binding = ActivityListNoteBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);



        //setup view adapter
        adapter = new NoteAdapter(this, notes, labels, username);
        tagAdapter = new TagAdapter(this, labels);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        user = FirebaseAuth.getInstance().getCurrentUser();
        //Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference().child(user.getUid());
    }

    private void initObject() {
        binding.recyclerViewNote.setAdapter(adapter);
        binding.recyclerViewTag.setAdapter(tagAdapter);
        mDatabase.child("notes").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Note note = snapshot.getValue(Note.class);
                notes.add(note);
                original.add(note);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Note note = snapshot.getValue(Note.class);

                for(int i = 0 ; i < original.size(); i++ ) {
                    if(original.get(i).getId().equals(note.getId()))
                        original.remove(i);
                }



                String labelNote = note.getLabel();
                //change number note of label
                for(int i = 0; i < labels.size(); i++ ){
                    String labelName = labels.get(i).getName();
                    Log.e("TAG", "onChildRemoved: " + labelName.equals(labelNote));
                    if(labelName.equals(labelNote)) {

                        int numberUpdate = labels.get(i).getNumber_note() - 1;
                        labels.get(i).setNumber_note(numberUpdate);

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("number_note", numberUpdate);

                        mDatabase.child("labels").child(labels.get(i).getId()).updateChildren(hashMap);
                    }
                }

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
                tagAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                tagAdapter.notifyDataSetChanged();
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


        //Action bar back
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    private void updateAdapterChange(){
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateUI(adapter.getItemCount());
            }
        });

    }

    private void updateUI (int count){
        if(count == 0 ){
            binding.textNote.setVisibility(View.VISIBLE);
        }else{
            binding.textNote.setVisibility(View.GONE);
        }

    }

    public void showListNoteByLabel(String name){
        notes.clear();
        if(name != null && !name.equals("note")){
            binding.allList.setVisibility(View.VISIBLE);
            binding.titleList.setText("List label: " + name );
            for(int i = 0 ; i < original.size(); i++){
                if(original.get(i).getLabel().equals(name)){
                    notes.add(original.get(i));
                }
            }
        }else if (name != null && name.equals("note")){
            binding.titleList.setText("List all notes " );
            binding.allList.setVisibility(View.GONE);
            notes.clear();
            notes.addAll(original);
        }

        adapter.notifyDataSetChanged();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.action_clear_all:
                ConfirmClearDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void ConfirmClearDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Clear");
        dialog.setMessage("Are you sure you want to put them all in the trash?");
        dialog.setNegativeButton("Cancel", null);
        dialog.setNeutralButton("OK", (dialogInterface, index) -> {


            for (int i = 0; i < notes.size(); i++){
                Note note = notes.get(i);
                mDatabase.child("trashes").child(note.getId()).setValue(note);
                mDatabase.child("notes").child(note.getId()).removeValue();
            }

            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("number_note", 0);

            for(int i = 0; i < labels.size() ; i++){
                mDatabase.child("labels").child(labels.get(i).getId()).updateChildren(hashMap);
            }

            Toast.makeText(this, "Move all complete", Toast.LENGTH_SHORT).show();
            notes.clear();

            tagAdapter.notifyDataSetChanged();
            adapter.notifyDataSetChanged();

        });
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Type title to search");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                notes.clear();
                for(int i = 0 ; i < original.size(); i++){
                    String value = original.get(i).getTitle();
                    if(value.contains(text)) {
                        notes.add(original.get(i));
                    }
                }

                adapter.notifyDataSetChanged();

                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }

}
