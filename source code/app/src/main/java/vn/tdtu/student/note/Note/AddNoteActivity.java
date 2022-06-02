package vn.tdtu.student.note.Note;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.tdtu.student.note.Label.Label;
import vn.tdtu.student.note.R;
import vn.tdtu.student.note.databinding.ActivityAddNoteBinding;
import vn.tdtu.student.note.databinding.ActivityListNoteBinding;

public class AddNoteActivity extends AppCompatActivity{
    private ActivityAddNoteBinding binding;

    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReferenceFromUrl("gs://complid-743fb.appspot.com");

    private final ArrayList<Note> notes = new ArrayList<>();
    private final ArrayList<Label> labels = new ArrayList<>();
    private final ArrayList<Note> original =  new ArrayList<>();

    private final int REQUEST_CODE_IMAGES = 100;

    Date date = new Date();
    Calendar calendar = Calendar.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        initObject();



        binding.textInputLabel.setOnFocusChangeListener((view, isFocus) -> {
            if(isFocus)
                LabelDialog();
        });

        binding.textInputTime.setOnFocusChangeListener((view, isFocus) -> {
            if(isFocus){
                TimeDialog();
            }
        });

        binding.textInputDay.setOnFocusChangeListener((view, isFocus) -> {
            if(isFocus)
                DayDialog();
        });

        binding.btnSaveNote.setOnClickListener(view -> addNote());

        binding.btnLoadImg.setOnClickListener(view -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_CODE_IMAGES);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_CODE_IMAGES && resultCode ==RESULT_OK && data != null){
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            binding.images.setImageBitmap(bitmap);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initView() {
        //binding
        binding = ActivityAddNoteBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
    }

    private void initObject() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        //Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference().child(user.getUid());
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

        //Action bar back
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


    private void addNote() {
        if(binding.textInputTitle.getText().length() == 0){
            binding.textLayoutTitle.setError("You must input your title ");

        }else if(binding.textInputContent.getText().length() == 0){
            binding.textLayoutContent.setError("You must input your content");

        }else{
            String id = mDatabase.push().getKey();
            String titleValue = binding.textInputTitle.getText().toString();
            String contentValue = binding.textInputContent.getText().toString();
            String labelValue = binding.textInputLabel.getText().toString();
            String dayValue = binding.textInputDay.getText().toString();
            String timeValue = binding.textInputTime.getText().toString();

            Note note = new Note(id, titleValue,  labelValue , contentValue, dayValue, timeValue, false, false, "", "");

            //Upload images
            Calendar calendar = Calendar.getInstance();
            StorageReference mountainsRef = storageRef.child("image" + calendar.getTimeInMillis() + ".png");

            binding.images.setDrawingCacheEnabled(true);
            binding.images.buildDrawingCache();

            if(binding.images.getDrawable() != null) {
                Bitmap bitmap = ((BitmapDrawable) binding.images.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = mountainsRef.putBytes(data);
                uploadTask.addOnFailureListener(exception -> {
                    // Handle unsuccessful uploads
                    Toast.makeText(AddNoteActivity.this, "Upload images faille", Toast.LENGTH_SHORT).show();
                }).addOnSuccessListener(taskSnapshot -> {
                    if (taskSnapshot.getMetadata() != null) {
                        if (taskSnapshot.getMetadata().getReference() != null) {
                            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                            result.addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                Map<String, Object> updateURL = new HashMap<String, Object>();
                                updateURL.put("imagesURL", imageUrl);
                                note.setImagesURL(imageUrl);
                                mDatabase.child("notes").child(id).updateChildren(updateURL);
                                Toast.makeText(AddNoteActivity.this, "Upload images success", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }

                });
            }


            //Upload notes

            mDatabase.child("notes").child(id).setValue(note).addOnCompleteListener(task -> {
                if(task.isComplete()) {
                    for(int i = 0; i < labels.size(); i++){
                        if(labels.get(i).getName().equals(labelValue)){
                            int num = labels.get(i).getNumber_note() + 1;
                            labels.get(i).setNumber_note(num);
                            HashMap<String, Object> hashMap  = new HashMap<>();
                            hashMap.put("number_note", num);
                            mDatabase.child("labels").child(labels.get(i).getId()).updateChildren(hashMap);
                        }
                    }

                    Toast.makeText(this, "Add success", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, NoteActivity.class);
                    startActivity(intent);
                    this.finish();
                }else{
                    Toast.makeText(this, "Something wrong", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void DayDialog() {
        calendar.setTime(date);
        DatePickerDialog dialog = new DatePickerDialog(this, (datePicker, year, month, day) -> {

            Date dateValue = new Date(year - 1900 , month, day);
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            String date = formatter.format(dateValue);
            binding.textInputDay.setText(date);

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void TimeDialog() {
        calendar.setTime(date);
        TimePickerDialog dialog = new TimePickerDialog(this, (timePicker, hour, minute) -> {

            Date timeValue = new Date(0, 0, 0, hour, minute);
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
            String time = formatter.format(timeValue);
            binding.textInputTime.setText(time);

        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        dialog.show();

    }

    private void LabelDialog() {
        List<String> labelList = new ArrayList<String>();
        for(int i = 0 ; i < labels.size() ; i++){
            labelList.add(labels.get(i).getName());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("List tag");
        builder.setSingleChoiceItems(labelList.toArray(new String[0]), 0, (dialogInterface, i) -> {
            String value = labels.get(i).getName().toString();
            binding.textInputLabel.setText(value);
            dialogInterface.dismiss();
        });
        builder.create().show();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



}
