package uk.ac.tees.aad.W9457747;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ListOFAvalilablity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // List to get the values
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_ofavalilablity);

        ListView listView = findViewById(R.id.listObj);

        ArrayList<String> values = new ArrayList<String>();
        ArrayAdapter<String> arrayAdapter  = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,values);
        listView.setAdapter(arrayAdapter);

       String data =  getIntent().getExtras().get("values").toString();


       for(String word:data.split(",")){
           values.add(word);
       }
       arrayAdapter.notifyDataSetChanged();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
