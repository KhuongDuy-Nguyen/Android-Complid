package vn.tdtu.student.note.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import vn.tdtu.student.note.MainActivity;
import vn.tdtu.student.note.R;
import vn.tdtu.student.note.databinding.ActivityLoginBinding;
import vn.tdtu.student.note.databinding.ActivityLoginRegisterBinding;

public class RegisterActivity extends AppCompatActivity {
    private ActivityLoginRegisterBinding binding;
    private FirebaseAuth mAuth;
    private ProgressDialog loader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initObject();

        binding.btnRegister.setOnClickListener(view1 -> signUp());

    }

    private void initObject(){
        binding = ActivityLoginRegisterBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        mAuth = FirebaseAuth.getInstance();
        loader = new ProgressDialog(this);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
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

    private void signUp(){
        String email = binding.textInputEmailRegister.getText().toString().trim();
        String password = binding.textInputPassword.getText().toString().trim();
        String passwordConfirm = binding.textInputPasswordConfirm.getText().toString();
        if (email.isEmpty() || !isValid(email)){
            binding.textLayoutEmailRegister.setError("Email required");
        }
        else if (password.length() < 6){
            binding.textInputPassword.setError("Password should be at least 6 characters");
        }
        else if(!password.matches(passwordConfirm)){
            binding.textLayoutPasswordConfirm.setError("Confirm password not match");
        }
        else {
            loader.setMessage("Registration in Progress");
            loader.setCanceledOnTouchOutside(false);
            loader.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, (OnCompleteListener<AuthResult>) task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Sign Up Success", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = mAuth.getCurrentUser();

                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        loader.dismiss();

                    } else {
                        String error = task.getException().getMessage();
                        Log.e("TAG", "Error: " + error );
                        Toast.makeText(RegisterActivity.this, "Sign Up fails: " + error, Toast.LENGTH_SHORT).show();
                        loader.dismiss();
                    }

                });

        }
    }

    static boolean isValid(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }

}
