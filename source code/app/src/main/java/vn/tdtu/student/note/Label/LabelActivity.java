package vn.tdtu.student.note.Label;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import vn.tdtu.student.note.Note.Note;
import vn.tdtu.student.note.R;
import vn.tdtu.student.note.databinding.ActivityListLabelBinding;

public class LabelActivity extends AppCompatActivity {
    private ActivityListLabelBinding binding;
    private DatabaseReference mDatabase;

    private final ArrayList<Label> labels = new ArrayList<>();
    private final ArrayList<Label> original =  new ArrayList<>();

    private LabelAdapter adapter;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_label);

        initView();
        updateAdapterChange();

        binding.btnAddLabel.setOnClickListener(view1 -> {
            AddDialog(this);
        });
    }

    private void initView() {
        //binding
        binding = ActivityListLabelBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);



        //setup view adapter
        adapter = new LabelAdapter(this, labels);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        binding.recyclerViewLabel.setLayoutManager(manager);
        binding.recyclerViewLabel.setHasFixedSize(true);
        binding.recyclerViewLabel.setAdapter(adapter);

        //Action bar back
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);



        //Firebase
        user = FirebaseAuth.getInstance().getCurrentUser();
        //Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference().child(user.getUid());
        mDatabase.child("labels").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Label label = snapshot.getValue(Label.class);
                labels.add(label);
                original.add(label);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void AddDialog(Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        dialog.setContentView(R.layout.dialog_label);

        TextInputEditText text = (TextInputEditText) dialog.findViewById(R.id.text_input_label);
        TextInputLayout layoutText = (TextInputLayout) dialog.findViewById(R.id.text_layout_label);

        TextView title = (TextView) dialog.findViewById(R.id.dialog_label_title);
        title.setText("Add Label");

        Button btn_save = (Button) dialog.findViewById(R.id.btn_save_label);
        Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel_label);

        btn_save.setOnClickListener(view1 -> {
            if(text.getText().length() == 0){
                layoutText.setError("You must input your label name");
            }
            else{
                //Add labels
                String id = mDatabase.push().getKey();
                String name = text.getText().toString();

                Label label = new Label(id, name, 0, 0);

                mDatabase.child("labels").child(id).setValue(label).addOnCompleteListener(task -> {
                    if(task.isComplete()) {
                        Toast.makeText(this, "Add success", Toast.LENGTH_SHORT).show();

                    }else{
                        Toast.makeText(this, "Something wrong", Toast.LENGTH_SHORT).show();
                    }
                });

                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

        btn_cancel.setOnClickListener(view12 -> {
            dialog.dismiss();
        });

        final Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);

        dialog.show();
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
        dialog.setMessage("Are you sure you want to clear all label?");
        dialog.setNegativeButton("Cancel", null);
        dialog.setNeutralButton("OK", (dialogInterface, index) -> {
            mDatabase.child("labels").removeValue();
            labels.clear();
            Toast.makeText(this, "Move all complete", Toast.LENGTH_SHORT).show();
            adapter.notifyDataSetChanged();
        });
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Type name to search");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                labels.clear();

                for(int i = 0 ; i < original.size(); i++){
                    String value = original.get(i).getName();
                    if(value.contains(text)) {
                        labels.add(original.get(i));
                    }
                }

                adapter.notifyDataSetChanged();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

}
