package vn.tdtu.student.note.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import vn.tdtu.student.note.MainActivity;
import vn.tdtu.student.note.databinding.ActivityLoginBinding;
import vn.tdtu.student.note.R;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private ProgressDialog loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initObject();


        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        binding.tvForgot.setOnClickListener(view1 -> {
            //Click forgot password
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        binding.tvRegister.setOnClickListener(view12 -> {
            //Click register
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });

        binding.btnLogin.setOnClickListener(view13 -> {
            Login();
        });
    }

    private void initObject(){
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        mAuth = FirebaseAuth.getInstance();
        loader = new ProgressDialog(this);

    }

    private void Login(){
        String email = binding.textInputEmail.getText().toString();
        String password = binding.textInputPassword.getText().toString();

        if (email.isEmpty() || !isValid(email)){
            binding.textLayoutEmail.setError("Email required");
        }
        else if (password.isEmpty()){
            binding.textLayoutPassword.setError("Password required");
        }else {
            loader.setMessage("Login in Progress");
            loader.setCanceledOnTouchOutside(false);
            loader.show();

            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this," Login success" ,  Toast.LENGTH_SHORT ).show();
                        loader.dismiss();
                        FirebaseUser user = mAuth.getCurrentUser();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(LoginActivity.this," Login failed. Try again ",  Toast.LENGTH_SHORT ).show();
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
