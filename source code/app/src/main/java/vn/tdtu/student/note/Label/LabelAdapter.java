package vn.tdtu.student.note.Label;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.provider.ContactsContract;
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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import vn.tdtu.student.note.R;
import java.util.ArrayList;
import java.util.HashMap;

public class LabelAdapter extends RecyclerView.Adapter<LabelAdapter.ViewHolder> {
    private DatabaseReference mDatabase;
    private ArrayList<Label> labels = new ArrayList<>();
    private Context context;
    private FirebaseUser user;

    public LabelAdapter(Context context, ArrayList<Label> labels) {
        this.labels = labels;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Firebase
        user = FirebaseAuth.getInstance().getCurrentUser();
        //Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child("labels");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_label, parent , false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Label label = labels.get(position);

        holder.label_name.setText(label.getName());

        holder.btn_delete.setOnClickListener(view -> {
            DeleteDialog(label, position);
        });

        holder.btn_edit.setOnClickListener(view -> {
            EditDialog(label, position);
        });
    }

    private void DeleteDialog(Label label, int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("Remove");
        dialog.setMessage("Are you sure you want to delete \"" + label.getName() + " \"");
        dialog.setNegativeButton("Cancel", null);
        dialog.setNeutralButton("OK", (dialogInterface, i) -> {

            mDatabase.child(label.getId()).removeValue().addOnCompleteListener(task -> {
                if(task.isComplete()) {
                    labels.remove(position);
                    Toast.makeText(context, "Remove complete", Toast.LENGTH_SHORT).show();
                }
                notifyItemRemoved(position);

            });
        });
        dialog.show();
    }

    private void EditDialog(Label label, int position){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        dialog.setContentView(R.layout.dialog_label);

        TextInputEditText name = (TextInputEditText) dialog.findViewById(R.id.text_input_label);
        TextView title = (TextView) dialog.findViewById(R.id.dialog_label_title);
        title.setText("Edit Label");
        name.setText(label.getName());

        Button btn_edit = (Button) dialog.findViewById(R.id.btn_save_label);
        Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel_label);

        btn_edit.setOnClickListener(v -> {

            String text = name.getText().toString();
            label.setName(text);

            //update firebase
            HashMap hashMap = new HashMap();
            hashMap.put("name", text);
            mDatabase.child(label.getId()).updateChildren(hashMap).addOnCompleteListener(task -> {
                if(task.isComplete()) {
                    Toast.makeText(context, "Update success", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(context, "Some thing wrong", Toast.LENGTH_SHORT).show();
            });

            notifyItemChanged(position);
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

    @Override
    public int getItemCount() {
        return labels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView btn_edit , btn_delete;
        TextView label_name;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            label_name = itemView.findViewById(R.id.label_name);
            btn_edit = itemView.findViewById(R.id.btn_edit_label);
            btn_delete = itemView.findViewById(R.id.btn_remove_label);
        }
    }
}
