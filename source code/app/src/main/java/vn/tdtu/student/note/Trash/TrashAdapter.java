package vn.tdtu.student.note.Trash;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import vn.tdtu.student.note.Note.Note;
import vn.tdtu.student.note.Note.NoteAdapter;
import vn.tdtu.student.note.R;

public class TrashAdapter extends RecyclerView.Adapter<TrashAdapter.ViewHolder> {

    private DatabaseReference mDatabase;
    private Context context;
    private FirebaseUser user;

    private ArrayList<Trash> trashes = new ArrayList<>();

    public TrashAdapter(Context context, ArrayList<Trash> trashes) {
        this.context = context;
        this.trashes = trashes;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference().child(user.getUid());
        View view = LayoutInflater.from(context).inflate(R.layout.list_trash, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trash trash = trashes.get(position);

        holder.title.setText(trash.getTitle());
        holder.content.setText(trash.getContent());


        String tag = trash.getLabel().toString();
        String day = trash.getDay().toString();
        String time = trash.getTime().toString();

        //label
        if(tag.isEmpty()){
            holder.viewTag.setVisibility(View.GONE);
        }else{
            holder.viewTag.setVisibility(View.VISIBLE);
            holder.textTag.setText(trash.getLabel());
        }

        //day
        if(day.isEmpty()){
            holder.viewDay.setVisibility(View.GONE);
        }else{
            holder.viewDay.setVisibility(View.VISIBLE);
            holder.textDay.setText(trash.getDay());
        }

        //time
        if(time.isEmpty()){
            holder.viewTime.setVisibility(View.GONE);
        }else{
            holder.viewTime.setVisibility(View.VISIBLE);
            holder.textTime.setText(trash.getTime());
        }

        holder.btn_restore.setOnClickListener(view -> {
            RestoreDialog(trash, position);
        });

        holder.btn_remove.setOnClickListener(view -> {
            DeleteDialog(trash, position);
        });
    }

    private void DeleteDialog(Trash trash, int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("Remove");
        dialog.setMessage("Are you sure you want to delete \"" + trash.getTitle() + " \"");
        dialog.setNegativeButton("Cancel", null);
        dialog.setNeutralButton("OK", (dialogInterface, index) -> {

            mDatabase.child("trashes").child(trash.getId()).removeValue().addOnCompleteListener(task -> {
                if(task.isComplete()) {
                    trashes.remove(position);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Remove complete", Toast.LENGTH_SHORT).show();
                }
            });

        });
        dialog.show();
    }

    private void RestoreDialog(Trash trash, int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("Restore");
        dialog.setMessage("Are you sure you want to restore \"" + trash.getTitle() + " \"");
        dialog.setNegativeButton("Cancel", null);
        dialog.setNeutralButton("OK", (dialogInterface, index) -> {

            mDatabase.child("notes").child(trash.getId()).setValue(trash);
            mDatabase.child("trashes").child(trash.getId()).removeValue().addOnCompleteListener(task -> {
                if(task.isComplete()) {
                    trashes.remove(position);
                    notifyItemRemoved(position);

                    Toast.makeText(context, "Restore complete", Toast.LENGTH_SHORT).show();
                }
            });

        });
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return trashes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, content;
        MaterialCardView viewTag, viewDay, viewTime;
        MaterialTextView textTag, textDay, textTime;
        ImageView btn_restore, btn_remove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.note_title);
            content = itemView.findViewById(R.id.note_content);

            viewTag = itemView.findViewById(R.id.view_tag);
            viewDay = itemView.findViewById(R.id.view_day);
            viewTime = itemView.findViewById(R.id.view_time);

            textTag = itemView.findViewById(R.id.note_tag);
            textDay = itemView.findViewById(R.id.note_day);
            textTime = itemView.findViewById(R.id.note_time);

            btn_remove = itemView.findViewById(R.id.btn_remove_trash);
            btn_restore = itemView.findViewById(R.id.btn_restore_trash);

        }
    }
}
