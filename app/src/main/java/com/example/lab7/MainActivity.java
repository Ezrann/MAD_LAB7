package com.example.lab7;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton fab;
    private RecyclerView recyclerView;
    private ArrayList<DataClass> dataList;
    private MyAdapter adapter;
    private HashMap<String, DataClass> keyDataMap;
    final private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Images");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dataList = new ArrayList<>();
        keyDataMap = new HashMap<>();
        adapter = new MyAdapter(this, dataList);
        recyclerView.setAdapter(adapter);

        adapter.setOnDeleteClickListener(position -> {
            if (position >= 0 && position < dataList.size()) {
                DataClass data = dataList.get(position);
                String key = getKeyForData(data);
                if (key != null) {
                    databaseReference.child(key).removeValue().addOnSuccessListener(aVoid -> {
                        Toast.makeText(MainActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    dataList.remove(position);
                    adapter.notifyItemRemoved(position);
                } else {
                    Toast.makeText(MainActivity.this, "Item not found in database", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Invalid item position", Toast.LENGTH_SHORT).show();
            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();
                keyDataMap.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    DataClass dataClass = dataSnapshot.getValue(DataClass.class);
                    String key = dataSnapshot.getKey();
                    if (dataClass != null && key != null) {
                        dataList.add(dataClass);
                        keyDataMap.put(key, dataClass);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, UploadActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private String getKeyForData(DataClass data) {
        if (data == null || keyDataMap == null) {
            return null;
        }
        for (HashMap.Entry<String, DataClass> entry : keyDataMap.entrySet()) {
            DataClass value = entry.getValue();
            if (value == null) {
                continue;
            }
            String imageURL = value.getImageURL();
            String caption = value.getCaption();
            String dataImageURL = data.getImageURL();
            String dataCaption = data.getCaption();
            // Handle null and equality checks safely
            boolean imageURLEquals = (imageURL == null && dataImageURL == null) ||
                    (imageURL != null && imageURL.equals(dataImageURL));
            boolean captionEquals = (caption == null && dataCaption == null) ||
                    (caption != null && caption.equals(dataCaption));
            if (imageURLEquals && captionEquals) {
                return entry.getKey();
            }
        }
        return null;
    }
}