package com.promactinfo.aliasagar.chatapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginOrRegisterActivity extends AppCompatActivity {
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_or_register);

        final EditText usernameTextView = (EditText) findViewById(R.id.username);
        final Button LoginOrRegisterButton= (Button) findViewById(R.id.login_register);

        LoginOrRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences=getSharedPreferences(getPackageName()+"Token_File", Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor=sharedPreferences.edit();
                ChatApplicationService CAS=ServiceBuilder.buildService(ChatApplicationService.class);
                String username=usernameTextView.getText().toString();
                if(username.isEmpty() || username.contains(" ")){
                    Toast.makeText(getApplicationContext(),"Invalid Username",Toast.LENGTH_SHORT).show();
                }
                else {
                    user = new User();
                    user.setName(username);
                    Call<User> call = CAS.Login(user);
                    call.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            editor.putString("token", response.body().getToken().toString());
                            editor.apply();
                            Intent intent = new Intent(LoginOrRegisterActivity.this, AllUsersActivity.class);
                            startActivity(intent);
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            Toast.makeText(getApplicationContext(), "Request Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                //Intent intent=new Intent(LoginOrRegisterActivity.this,AllUsersActivity.class);
                //startActivity(intent);
            }
        });
    }
}
