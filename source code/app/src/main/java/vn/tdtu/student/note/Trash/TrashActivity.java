package vn.tdtu.student.note.Trash;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

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
import vn.tdtu.student.note.Note.Note;
import vn.tdtu.student.note.Note.NoteAdapter;
import vn.tdtu.student.note.Note.TagAdapter;
import vn.tdtu.student.note.R;
import vn.tdtu.student.note.databinding.ActivityListNoteBinding;
import vn.tdtu.student.note.databinding.ActivityListTrashBinding;

public class TrashActivity extends AppCompatActivity {
    private ActivityListTrashBinding binding;

    private DatabaseReference mDatabase;

    private final ArrayList<Trash> trashes = new ArrayList<>();
    private final ArrayList<Trash> original =  new ArrayList<>();
    private TrashAdapter adapter;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        initObject();
        updateAdapterChange();
    }

    private void initView() {
        //binding
        binding = ActivityListTrashBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);



        //setup view adapter
        adapter = new TrashAdapter(this, trashes);
        binding.recyclerViewTrash.setAdapter(adapter);

        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference().child(user.getUid());
    }

    private void initObject() {


        mDatabase.child("trashes").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Trash trash = snapshot.getValue(Trash.class);
                trashes.add(trash);
                original.add(trash);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Trash trash = snapshot.getValue(Trash.class);

                for(int i = 0 ; i < original.size(); i++ ) {
                    if(original.get(i).getId().equals(trash.getId()))
                        original.remove(i);
                }

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
        dialog.setMessage("Are you sure you want to remove all?");
        dialog.setNegativeButton("Cancel", null);
        dialog.setNeutralButton("OK", (dialogInterface, index) -> {
            mDatabase.child("trashes").removeValue();
            trashes.clear();
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Remove success", Toast.LENGTH_SHORT).show();
        });
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Input TITLE to search");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                trashes.clear();
                for(int i = 0 ; i < original.size(); i++){
                    String value = original.get(i).getTitle();
                    if(value.contains(text)) {
                        trashes.add(original.get(i));
                    }
                }

                adapter.notifyDataSetChanged();

                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }


}
