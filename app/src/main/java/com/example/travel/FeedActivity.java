package com.example.travel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;
public class FeedActivity extends AppCompatActivity {

    private static final String TAG = "DocSnippets";
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;


    ArrayList<String> userEmailFromFB;
    ArrayList<String> userCommentFromFB;
    ArrayList<String> userImageFromFB;
    ArrayList<String> userMapsFB;
    FeedRecyclerAdapter feedRecyclerAdapter;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.menu_travel,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.add_post){
            Intent intent = new Intent(FeedActivity.this,UploadActivity.class);
            startActivity(intent);

        } else if(item.getItemId() == R.id.signout){

            firebaseAuth.signOut();

            Intent intentToSignUp =new Intent(FeedActivity.this,SignUpActivity.class);
            startActivity(intentToSignUp);


        }

        return super.onOptionsItemSelected(item);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);


        userCommentFromFB = new ArrayList<>();
        userEmailFromFB =new ArrayList<>();
        userImageFromFB = new ArrayList<>();
        userMapsFB =new ArrayList<>();

        firebaseAuth =FirebaseAuth.getInstance();
        firebaseFirestore =FirebaseFirestore.getInstance();


        getDataFromstore();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this ));

        feedRecyclerAdapter = new FeedRecyclerAdapter(userEmailFromFB,userCommentFromFB,userImageFromFB,userMapsFB);
        recyclerView.setAdapter(feedRecyclerAdapter);

        ItemTouchHelper helper =new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder target, int direction) {
                int position =target.getAdapterPosition();
                userImageFromFB.remove(position);
                userCommentFromFB.remove(position);
                userEmailFromFB.remove(position);
                feedRecyclerAdapter.notifyDataSetChanged();

            }
        });
        helper.attachToRecyclerView(recyclerView);


    }






    public void getDataFromstore(){
        CollectionReference collectionReference =firebaseFirestore.collection("Posts");
        collectionReference.orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if( e != null){
                    Toast.makeText(FeedActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();

                }
                if(queryDocumentSnapshots != null){
                    for(DocumentSnapshot snapshot: queryDocumentSnapshots.getDocuments()){
                        Map<String ,Object> data =snapshot.getData();
                        String comment = (String)data.get("comment");
                        String userEmail =(String)data.get("useremail");
                        String downloadUrl =(String)data.get("downloadurl");
                        String place =(String)data.get("place");


                        userCommentFromFB.add(comment);
                        userEmailFromFB.add(userEmail);
                        userImageFromFB.add(downloadUrl);
                        userMapsFB.add(place);


                        feedRecyclerAdapter.notifyDataSetChanged();
                    }
                }


            }
        });
    }


}
