package com.promactinfo.aliasagar.chatapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
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
    ArrayList<Message> messages;
    ChatApplicationService CAS;
    String token;
    int userId=-1;
    int size=0;
    Handler h = new Handler();
    int delay = 1*1000; //1 second=1000 milisecond, 15*1000=15seconds
    Runnable runnable;
    @Override
    protected void onResume() {
        //start handler as activity become visible

        h.postDelayed( runnable = new Runnable() {
            public void run() {
                //do something
                getMessages();
                h.postDelayed(runnable, delay);
            }
        }, delay);

        super.onResume();
    }

    @Override
    protected void onPause() {
        h.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        //autoSync=new AutoSync();
        //autoSync.onResume();
        messages=new ArrayList<>();
        final SharedPreferences sharedPreferences = getSharedPreferences(getPackageName() + "Token_File", Context.MODE_PRIVATE);
        CAS = ServiceBuilder.buildService(ChatApplicationService.class);
        token = sharedPreferences.getString("token", "N/A");
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
                int selectedItem = (int) parent.getItemIdAtPosition(position);
                userId = UserDetails.get(selectedItem).getId();
                getMessages();
                Button sendButton=(Button)findViewById(R.id.send_button);
                final EditText message_content=(EditText)findViewById(R.id.editText2);
                final Gson gson=new Gson();

                SharedPreferences sp = getSharedPreferences(getPackageName() + "Messages", Context.MODE_PRIVATE);
                String json=sp.getString("messages","null");
                Type type = new TypeToken<ArrayList<Message>>() {}.getType();
                ArrayList<Message> msgs = gson.fromJson(json,type);
                if(msgs!=null) {
                    for (int i = 0; i < msgs.size(); i++) {
                        sendMsg(msgs.get(i));
                    }
                    msgs.clear();
                    SharedPreferences.Editor editor = sp.edit();
                    String jsons = gson.toJson(msgs);
                    editor.putString("messages", jsons);
                    editor.apply();
                }

                sendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String text = message_content.getText().toString().trim();
                        //Toast.makeText(getApplicationContext(), "online", Toast.LENGTH_SHORT).show();
                        if (text.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Message should not empty.", Toast.LENGTH_LONG).show();
                        }
                        else {
                            Message message=new Message();
                            message.setMessage(message_content.getText().toString());
                            message.setToUserId(userId);
                            if (isOnline()) {
                                SharedPreferences sp = getSharedPreferences(getPackageName() + "Messages", Context.MODE_PRIVATE);
                                String json=sp.getString("messages","null");
                                Type type = new TypeToken<ArrayList<Message>>() {}.getType();
                                ArrayList<Message> msgs = gson.fromJson(json,type);
                                if(msgs!=null) {
                                    for (int i = 0; i < msgs.size(); i++) {
                                        sendMsg(msgs.get(i));
                                    }
                                    msgs.clear();
                                    SharedPreferences.Editor editor = sp.edit();
                                    String jsons = gson.toJson(msgs);
                                    editor.putString("messages", jsons);
                                    editor.apply();
                                }
                                sendMsg(message);
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "You're offline, Message will send once you'll come online", Toast.LENGTH_LONG).show();
                                SharedPreferences sp = getSharedPreferences(getPackageName() + "Messages", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor=sp.edit();
                                messages.add(message);
                                String json=gson.toJson(messages);
                                editor.putString("messages", json);
                                editor.apply();
                            }
                        }
                        message_content.setText("");
                    }
                });
            }
        });
    }

    private void sendMsg(Message message) {
        Call<Void> send_msg = CAS.sendMessage(message, token);
        send_msg.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(getApplicationContext(), "Sent", Toast.LENGTH_SHORT).show();
                getMessages();
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Request Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMessages() {
        Call<List<Message>> msg = CAS.getUserMessages(userId, token);
        msg.enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                String userMessages[] = new String[response.body().size()];
                if(userMessages.length!=size) {
                    size=userMessages.length;
                    for (int i = 0; i < response.body().size(); i++) {
                        userMessages[i] = response.body().get(i).getFromUserId() + " : " + response.body().get(i).getMessage();
                    }
                    MessageList = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, userMessages);
                    UserMessages.setAdapter(MessageList);
                    if(size > 9) {
                        UserMessages.setSelection(UserMessages.getAdapter().getCount() - 1);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                //Toast.makeText(getApplicationContext(), "Request Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean isOnline() {
        ConnectivityManager connectivityMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }
}
