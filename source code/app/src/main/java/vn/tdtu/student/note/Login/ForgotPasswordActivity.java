package vn.tdtu.student.note.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import vn.tdtu.student.note.R;
import vn.tdtu.student.note.databinding.ActivityLoginBinding;
import vn.tdtu.student.note.databinding.ActivityLoginForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityLoginForgotPasswordBinding binding;

    private FirebaseAuth mAuth;
    private ProgressDialog loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_forgot_password);

        initObject();



        binding.btnSendEmail.setOnClickListener(view -> sendEmail());
    }
    private void initObject(){
        binding = ActivityLoginForgotPasswordBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mAuth = FirebaseAuth.getInstance();
        loader = new ProgressDialog(this);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

    }


    private void sendEmail() {

        String email = binding.textInputEmailForgot.getText().toString();
        if(email.isEmpty() || !isValid(email)){
            Toast.makeText(ForgotPasswordActivity.this, "Email not valid", Toast.LENGTH_SHORT).show();
        }else{
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this, "Email have send", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                });
        }
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

    static boolean isValid(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }
}
