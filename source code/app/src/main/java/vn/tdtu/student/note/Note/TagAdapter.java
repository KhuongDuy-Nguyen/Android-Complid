package vn.tdtu.student.note.Note;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import vn.tdtu.student.note.Label.Label;
import vn.tdtu.student.note.MainActivity;
import vn.tdtu.student.note.R;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Label> labels = new ArrayList<>();

    public TagAdapter(Context context, ArrayList<Label> labels) {
        this.context = context;
        this.labels = labels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_tag, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Label label = labels.get(position);

        String num_note = String.valueOf(label.getNumber_note());
        String num_done = String.valueOf(label.getNumber_note_done());

        holder.title.setText(label.getName());
        holder.num_note.setText(num_note);
        holder.num_done.setText(num_done);



        holder.itemView.setOnClickListener(view -> {
            String name = label.getName();
            if(context instanceof NoteActivity){
                ((NoteActivity) context).showListNoteByLabel(name);
            }
        });
    }

    @Override
    public int getItemCount() {
        return labels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, num_note, num_done;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title_tag);
            num_note = itemView.findViewById(R.id.number_total_note);
            num_done = itemView.findViewById(R.id.number_total_done);
        }
    }
}
