package com.athasamid.detail.image.from.listview;

import android.content.Context;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView mListView;
    private ArrayList<User> userLists = new ArrayList<>();
    private MyCustomListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpViews();
    }

    private void setUpViews() {
        mListView = findViewById(R.id.listView);
        adapter = new MyCustomListAdapter(this);
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showDialog(userLists.get(position));
            }
        });

        loadData();
    }

    private void loadData() {
        AndroidNetworking.get("https://reqres.in/api/users")
                .addQueryParameter("per_page", "10")
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Better use gson or jackson
                        try {
                            if (response.has("data")){
                                JSONArray data = response.getJSONArray("data");
                                for (int i =0; i< data.length(); i++){
                                    JSONObject currData = data.getJSONObject(i);
                                    User newUser = new User();
                                    newUser.setId(currData.getInt("id"));
                                    newUser.setFirstName(currData.getString("first_name"));
                                    newUser.setLastName(currData.getString("last_name"));
                                    newUser.setAvatar(currData.getString("avatar"));
                                    userLists.add(newUser);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }

    private void showDialog(User user){
        if (user == null)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_show_detail, null);
        builder.setView(view);

        ImageView avatar = view.findViewById(R.id.avatar);
        TextView nama = view.findViewById(R.id.name);

        Glide.with(this).load(user.getAvatar()).into(avatar);
        nama.setText(String.format("%s %s", user.getFirstName(), user.getLastName()));
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private class MyCustomListAdapter extends ArrayAdapter<User>{
        Context mContext;
        public MyCustomListAdapter(Context context){
            super(context, R.layout.item_user, userLists);
            this.mContext = context;
        }

        @Override
        public long getItemId(int position) {
            return userLists.get(position).getId();
        }

        @Nullable
        @Override
        public User getItem(int position) {
            return userLists.get(position);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            User user = getItem(position);
            ViewHolder viewHolder;

            if (convertView == null){
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_user, parent, false);
                viewHolder.avatar = convertView.findViewById(R.id.avatar);
                viewHolder.firstName = convertView.findViewById(R.id.firstName);
                viewHolder.lastName = convertView.findViewById(R.id.lastName);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // Binding data
            viewHolder.lastName.setText(user.getFirstName());
            viewHolder.firstName.setText(user.getLastName());

            Glide.with(mContext).load(user.getAvatar()).into(viewHolder.avatar);

            return convertView;
        }

        class ViewHolder{
            TextView firstName;
            TextView lastName;
            ImageView avatar;
        }
    }
}
