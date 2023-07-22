package com.example.check;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, confirmPasswordEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        Button signupButton = findViewById(R.id.signupButton);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPassword)) {
                    if (password.equals(confirmPassword)) {
                        createUser(email, password);
                    } else {
                        Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SignupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createUser(String email, String password) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Signup successful, navigate to the chat activity
                            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Signup failed, display an error message
                            Toast.makeText(SignupActivity.this, "Signup failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
}
