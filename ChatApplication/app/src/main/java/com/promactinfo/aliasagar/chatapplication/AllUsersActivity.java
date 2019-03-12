package com.promactinfo.aliasagar.chatapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllUsersActivity extends AppCompatActivity {
    ListView AllUsers;
    ListView UserMessages;
    ArrayList<User> UserDetails;
    ArrayAdapter<String> UserList;
    ArrayAdapter<String> MessageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName() + "Token_File", Context.MODE_PRIVATE);
        final ChatApplicationService CAS = ServiceBuilder.buildService(ChatApplicationService.class);
        final String token = sharedPreferences.getString("token", "N/A");
        Call<List<User>> call = CAS.getAllUsers(token);
        AllUsers = (ListView) findViewById(R.id.user_list);
        UserMessages = (ListView) findViewById(R.id.messages);
        UserDetails = new ArrayList<>();
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                String users[] = new String[response.body().size()];
                for (int i = 0; i < response.body().size(); i++) {
                    users[i] = response.body().get(i).getName();
                    UserDetails.add(response.body().get(i));
                }
                UserList = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, users);
                AllUsers.setAdapter(UserList);
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Request Failed", Toast.LENGTH_SHORT).show();
            }
        });

        AllUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item text from ListView
                int selectedItem = (int) parent.getItemIdAtPosition(position);
                final int userId = UserDetails.get(selectedItem).getId();
                //Toast.makeText(getApplicationContext(), userId + "", Toast.LENGTH_SHORT).show();
                getMessages(CAS,userId,token);

                Button sendButton=(Button)findViewById(R.id.send_button);
                final EditText message_content=(EditText)findViewById(R.id.editText2);
                final Message message=new Message();
                sendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String text = message_content.getText().toString().trim();
                        if(text.isEmpty()){
                            Toast.makeText(getApplicationContext(),"Message should not empty.",Toast.LENGTH_LONG).show();
                        }
                        else{
                            message.setMessage(message_content.getText().toString());
                            message.setToUserId(userId);
                            Call<Void> send_msg=CAS.sendMessage(message,token);
                            send_msg.enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    Toast.makeText(getApplicationContext(), "Sent", Toast.LENGTH_SHORT).show();
                                    getMessages(CAS,userId,token);
                                }
                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(getApplicationContext(), "Request Failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        message_content.setText("");
                    }
                });
            }
        });
    }

    private void getMessages(ChatApplicationService CAS, int userId, String token) {
        Call<List<Message>> msg = CAS.getUserMessages(userId, token);
        msg.enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                String userMessages[] = new String[response.body().size()];
                for (int i = 0; i < response.body().size(); i++) {
                    userMessages[i] = response.body().get(i).getFromUserId() + " : " + response.body().get(i).getMessage();
                }
                MessageList = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, userMessages);
                UserMessages.setAdapter(MessageList);
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Request Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
