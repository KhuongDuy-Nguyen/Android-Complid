package vn.tdtu.student.note.Note;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import vn.tdtu.student.note.Label.Label;
import vn.tdtu.student.note.R;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {

    private DatabaseReference mDatabase;
    private Context context;
    private String username;

    private ArrayList<Note> notes = new ArrayList<>();
    private ArrayList<Label> labels = new ArrayList<>();
    private FirebaseUser user;


    Date date = new Date();
    Calendar calendar = Calendar.getInstance();

    boolean isPin;


    public NoteAdapter(Context context, ArrayList<Note> notes, ArrayList<Label> labels, String username) {
        this.context = context;
        this.notes = notes;
        this.labels = labels;
        this.username = username;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference().child(user.getUid());
        View view = LayoutInflater.from(context).inflate(R.layout.list_note, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.title.setText(note.getTitle());
        holder.content.setText(note.getContent());


        String tag = note.getLabel().toString();
        String day = note.getDay().toString();
        String time = note.getTime().toString();

        //label
        if(tag.isEmpty()){
            holder.viewTag.setVisibility(View.GONE);
        }else{
            holder.viewTag.setVisibility(View.VISIBLE);
            holder.textTag.setText(note.getLabel());
        }

        //day and time
        if(day.isEmpty() && time.isEmpty()){
            holder.viewDay.setVisibility(View.GONE);
        }else{
            holder.viewDay.setVisibility(View.VISIBLE);
            String datetime = note.getDay() +  " " + note.getTime();
            holder.textDay.setText(datetime);
        }


        if(note.getImagesURL() == null || note.getImagesURL().isEmpty()){
            holder.imageView.setVisibility(View.GONE);
        }else{
            holder.imageView.setVisibility(View.VISIBLE);
            Picasso.with(context).load(note.getImagesURL()).into(holder.imageView);
        }


        holder.btn_edit.setOnClickListener(view -> {
            EditDialog(note, position);
        });

        holder.btn_remove.setOnClickListener(view -> {
            DeleteDialog(note, position);
        });


        isPin = true;
        holder.itemView.setOnClickListener(view -> {
            if(isPin) {
                holder.pin.setVisibility(View.VISIBLE);
                swapItem(position, 0);
            }else{
                holder.pin.setVisibility(View.GONE);
                swapItem(position, 0);
            }
            isPin = !isPin;
        });




        if(note.isLock()){
            changVisibility(holder, true);
        }else{
            changVisibility(holder, false);
        }

        holder.btn_lock.setOnClickListener(view -> {
            if(note.isLock() && note.getPassword().length() > 0){

                Log.e("TAG", "OpenPassword");
                OpenPasswordDialog(note, position);

            }else if(note.getPassword().length() > 0 && !note.isLock){

                Log.e("TAG", "ChangePassword");
                ChangePassword(note , position);

            }else{

                Log.e("TAG", "SetPassword");
                SetPasswordDialog(note , position);

            }

        });

        holder.btn_share.setOnClickListener(view -> {
            Intent sendIntent = new Intent();

            String content = "";
            if(note.getDay().isEmpty() && note.getTime().isEmpty()){
                 content = note.getTitle() + ": " + note.getContent();
            }else if(note.getDay().isEmpty() || note.getTime().isEmpty()){
                 content = note.getTitle() + ": " + note.getContent() + " vao luc " + note.getDay() + note.getTime();
            }else{
                 content = note.getTitle() + ": " + note.getContent() + " vao ngay " + note.getDay() + " luc " + note.getTime();
            }
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, content);
            sendIntent.putExtra(Intent.EXTRA_TITLE, note.getTitle());

            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            view.getContext().startActivity(shareIntent);
        });


    }

    private void changVisibility(ViewHolder holder, boolean hide){
        if(hide){
            holder.btn_lock.setImageResource(R.drawable.ic_lock);
            holder.btn_edit.setVisibility(View.GONE);
            holder.btn_remove.setVisibility(View.GONE);
            holder.content.setVisibility(View.GONE);
            holder.title.setVisibility(View.GONE);
            holder.btn_share.setVisibility(View.GONE);

            if(holder.viewTag.getVisibility() == View.VISIBLE)
                holder.viewTag.setVisibility(View.GONE);

            if(holder.viewDay.getVisibility() == View.VISIBLE)
                holder.viewDay.setVisibility(View.GONE);

            if(holder.imageView.getVisibility() == View.VISIBLE)
                holder.imageView.setVisibility(View.GONE);

        }else{
            holder.btn_lock.setImageResource(R.drawable.ic_lock_open);
            holder.btn_edit.setVisibility(View.VISIBLE);
            holder.btn_remove.setVisibility(View.VISIBLE);
            holder.content.setVisibility(View.VISIBLE);
            holder.title.setVisibility(View.VISIBLE);
            holder.btn_share.setVisibility(View.VISIBLE);
        }

    }


    private void swapItem(int fromPosition, int toPosition){
        Collections.swap(notes, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    private void ChangePassword(Note note, int position) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        dialog.setContentView(R.layout.dialog_edit_password);

        TextInputEditText oldPass = (TextInputEditText) dialog.findViewById(R.id.text_input_old_pass);
        TextInputLayout layoutOldPass = (TextInputLayout) dialog.findViewById(R.id.text_layout_old_pass);

        TextInputEditText newPass = (TextInputEditText) dialog.findViewById(R.id.text_input_new_pass);
        TextInputLayout layoutNewPass = (TextInputLayout) dialog.findViewById(R.id.text_layout_new_pass);

        TextInputEditText confNewPass = (TextInputEditText) dialog.findViewById(R.id.text_input_confirm_new_password);
        TextInputLayout layoutConfNewPass = (TextInputLayout) dialog.findViewById(R.id.text_layout_confirm_new_password);



        Button btn_change = (Button) dialog.findViewById(R.id.btn_change_password);
        Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel_password);
        Button btn_lock = (Button) dialog.findViewById(R.id.btn_lock);

        btn_change.setOnClickListener(v -> {
            String passCheck = note.getPassword();
            String passOld = oldPass.getText().toString();
            String passNew = newPass.getText().toString();
            String confirmNewPass = confNewPass.getText().toString();

            if(passCheck.matches(passOld)){
                if(passNew.isEmpty()){
                    layoutNewPass.setError("You must input new password");
                }else if(confirmNewPass.isEmpty()){
                    layoutConfNewPass.setError("You must input confirm new password");
                }else if(!passNew.matches(confirmNewPass)){
                    layoutConfNewPass.setError("Confirm password not match");
                }else{

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("lock", true);
                    hashMap.put("password", passNew);

                    mDatabase.child("notes").child(note.getId()).updateChildren(hashMap).addOnCompleteListener(task -> {
                        if (task.isComplete()) {
                            note.setLock(true);
                            note.setPassword(passNew);
                            Toast.makeText(context, "Change password success", Toast.LENGTH_SHORT).show();
                            notifyItemChanged(position);
                        }
                    });

                    dialog.dismiss();
                }
            }else{
                layoutOldPass.setError("Your password not correct");
            }
        });

        btn_lock.setOnClickListener(view -> {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("lock", true);

            mDatabase.child("notes").child(note.getId()).updateChildren(hashMap).addOnCompleteListener(task -> {
                if (task.isComplete()) {
                    note.setLock(true);
                    Toast.makeText(context, "Lock success", Toast.LENGTH_SHORT).show();
                    notifyItemChanged(position);
                }
            });

            dialog.dismiss();

        });

        btn_cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        final Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);

        dialog.show();
    }

    private void OpenPasswordDialog(Note note, int position) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        dialog.setContentView(R.layout.dialog_password);

        TextInputEditText pass = (TextInputEditText) dialog.findViewById(R.id.text_input_pass);
        TextInputLayout layoutPass = (TextInputLayout) dialog.findViewById(R.id.text_layout_pass);
        TextInputLayout layoutConfPass = (TextInputLayout) dialog.findViewById(R.id.text_layout_confirm_password);

        TextView title = (TextView) dialog.findViewById(R.id.dialog_title);
        title.setText("Unlock password");
        layoutConfPass.setVisibility(View.GONE);

        Button btn_apply = (Button) dialog.findViewById(R.id.btn_apply_password);
        Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel_password);

        btn_apply.setOnClickListener(v -> {

            String passValue = pass.getText().toString();

            if(passValue.length() == 0) {
                layoutPass.setError("You must input your password");
            }else{
                String passCheck = note.getPassword().toString();

                if(passCheck.matches(passValue)){

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("lock", false);

                    mDatabase.child("notes").child(note.getId()).updateChildren(hashMap).addOnCompleteListener(task -> {
                        if (task.isComplete()) {
                            note.setLock(false);
                            Toast.makeText(context, "Unlock success", Toast.LENGTH_SHORT).show();
                            notifyItemChanged(position);
                        }
                    });

                    dialog.dismiss();
                }else{
                    layoutPass.setError("Your password not correct");
                }
            }
        });

        btn_cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        final Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);

        dialog.show();
    }

    private void SetPasswordDialog(Note note, int position){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        dialog.setContentView(R.layout.dialog_password);

        TextInputEditText pass = (TextInputEditText) dialog.findViewById(R.id.text_input_pass);
        TextInputEditText confPass = (TextInputEditText) dialog.findViewById(R.id.text_input_confirm_password);

        TextInputLayout layoutPass = (TextInputLayout) dialog.findViewById(R.id.text_layout_pass);
        TextInputLayout layoutConfPass = (TextInputLayout) dialog.findViewById(R.id.text_layout_confirm_password);

        TextView title = (TextView) dialog.findViewById(R.id.dialog_title);
        title.setText("Set up password");

        Button btn_apply = (Button) dialog.findViewById(R.id.btn_apply_password);
        Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel_password);

        btn_apply.setOnClickListener(v -> {

            String passValue = pass.getText().toString();
            String confirmValue = confPass.getText().toString();

            if(passValue.length() == 0) {
                layoutPass.setError("You must input your password");
            }

            else if(confirmValue.length() == 0) {
                layoutConfPass.setError("You must confirm your password");
            }

            else if(!passValue.matches(confirmValue)) {
                layoutConfPass.setError("The confirm password mot match");
            }

            else{
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("lock", true);
                hashMap.put("password", passValue);

                mDatabase.child("notes").child(note.getId()).updateChildren(hashMap).addOnCompleteListener(task -> {
                    if (task.isComplete()) {
                        note.setLock(true);
                        note.setPassword(passValue);
                        Toast.makeText(context, "Setup lock success", Toast.LENGTH_SHORT).show();
                        notifyItemChanged(position);
                    }
                });
                dialog.dismiss();
            }


        });

        btn_cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        final Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);

        dialog.show();
    }

    private void DeleteDialog(Note note, int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("Remove");
        dialog.setMessage("Are you sure you want to move \"" + note.getTitle() + " \" to trash");
        dialog.setNegativeButton("Cancel", null);
        dialog.setNeutralButton("OK", (dialogInterface, index) -> {

            mDatabase.child("trashes").child(note.getId()).setValue(note);

            mDatabase.child("notes").child(note.getId()).removeValue().addOnCompleteListener(task -> {
                if(task.isComplete()) {
                    notes.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Remove complete", Toast.LENGTH_SHORT).show();
                }
            });


        });
        dialog.show();
    }

    private void EditDialog(Note note, int position) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        dialog.setContentView(R.layout.dialog_note);
        TextView title = (TextView) dialog.findViewById(R.id.dialog_note_title);
        TextInputEditText textTitle = (TextInputEditText) dialog.findViewById(R.id.text_input_title);
        TextInputLayout layoutTitle = (TextInputLayout) dialog.findViewById(R.id.text_layout_title);
        TextInputEditText textContent = (TextInputEditText) dialog.findViewById(R.id.text_input_content);
        TextInputLayout layoutContent = (TextInputLayout) dialog.findViewById(R.id.text_layout_content);
        TextInputEditText textLabel = (TextInputEditText) dialog.findViewById(R.id.text_input_label);
        TextInputEditText textDay = (TextInputEditText) dialog.findViewById(R.id.text_input_day);
        TextInputEditText textTime = (TextInputEditText) dialog.findViewById(R.id.text_input_time);

        Button btn_save = (Button) dialog.findViewById(R.id.btn_save_note);
        Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel_note);

        title.setText("Edit Note");
        textTitle.setText(note.getTitle());
        textContent.setText(note.getContent());
        textLabel.setText(note.getLabel());
        textDay.setText(note.getDay());
        textTime.setText(note.getTime());

        textLabel.setOnFocusChangeListener((view, isFocus) -> {
            if(isFocus)
                LabelDialog(textLabel);
        });


        textTime.setOnFocusChangeListener((view, isFocus) -> {
            if(isFocus)
                TimeDialog(textTime);
        });

        textDay.setOnFocusChangeListener((view, isFocus) -> {
            if(isFocus)
                DayDialog(textDay);
        });

        btn_save.setOnClickListener(view1 -> {
            if(textTitle.getText().length() == 0){
                layoutTitle.setError("You must input your title ");
            }else if(textContent.getText().length() == 0){
                layoutContent.setError("You must input your content");
            }else{
                //Edit notes
                String titleValue = textTitle.getText().toString();
                String contentValue = textContent.getText().toString();
                String labelValue = textLabel.getText().toString();
                String dayValue = textDay.getText().toString();
                String timeValue = textTime.getText().toString();

                note.setTitle(titleValue);
                note.setContent(contentValue);
                note.setLabel(labelValue);
                note.setDay(dayValue);
                note.setTime(timeValue);

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("title", titleValue);
                hashMap.put("content", contentValue);
                hashMap.put("label", labelValue);
                hashMap.put("day", dayValue);
                hashMap.put("time", timeValue);

                mDatabase.child("notes").child(note.getId()).updateChildren(hashMap).addOnCompleteListener(task -> {
                    if(task.isComplete()) {
                        Toast.makeText(context, "Update success", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(context, "Some thing wrong", Toast.LENGTH_SHORT).show();
                });

                notifyItemChanged(position);
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

    private void DayDialog(TextInputEditText textDay) {
        calendar.setTime(date);
        DatePickerDialog dialog = new DatePickerDialog(context, (datePicker, year, month, day) -> {

            Date dateValue = new Date(year - 1900 , month, day);
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            String date = formatter.format(dateValue);
            textDay.setText(date);

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void TimeDialog(TextInputEditText textTime) {
        calendar.setTime(date);
        TimePickerDialog dialog = new TimePickerDialog(context, (timePicker, hour, minute) -> {

            Date timeValue = new Date(0, 0, 0, hour, minute);
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
            String time = formatter.format(timeValue);
            textTime.setText(time);

        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        dialog.show();
    }

    private void LabelDialog(TextInputEditText textLabel) {
        List<String> labelList = new ArrayList<String>();
        for(int i = 0 ; i < labels.size() ; i++){
            labelList.add(labels.get(i).getName());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("List tag");
        builder.setSingleChoiceItems(labelList.toArray(new String[0]), 1, (dialogInterface, i) -> {
            String value = labels.get(i).getName().toString();
            textLabel.setText(value);
            dialogInterface.dismiss();
        });
        builder.create().show();
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, content;
        MaterialCardView viewTag, viewDay;
        MaterialTextView textTag, textDay;
        ImageView btn_edit, btn_remove, btn_lock ,pin, btn_share, imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.note_title);
            content = itemView.findViewById(R.id.note_content);

            viewTag = itemView.findViewById(R.id.view_tag);
            viewDay = itemView.findViewById(R.id.view_day);

            textTag = itemView.findViewById(R.id.note_tag);
            textDay = itemView.findViewById(R.id.note_day);

            btn_remove = itemView.findViewById(R.id.btn_remove_note);
            btn_edit = itemView.findViewById(R.id.btn_edit_note);
            btn_lock = itemView.findViewById(R.id.btn_lock);
            btn_share = itemView.findViewById(R.id.btn_share_note);
            imageView  = itemView.findViewById(R.id.imageView);
            pin = itemView.findViewById(R.id.pin);

        }
    }
}
