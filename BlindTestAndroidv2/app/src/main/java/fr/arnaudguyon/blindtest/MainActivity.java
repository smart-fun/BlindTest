package fr.arnaudguyon.blindtest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import fr.arnaudguyon.blindtest.spotify.SpotConst;
import fr.arnaudguyon.blindtest.spotify.SpotPlay;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        findViewById(R.id.connect_spotify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectSpotify();
            }
        });

    }

    private void connectSpotify() {
        SpotPlay spotPlay = SpotPlay.getInstance();
        spotPlay.connect(this, new SpotPlay.SpotConnectListener() {
            @Override
            public void onSpotConnection(boolean success) {
                Log.i(SpotConst.TAG, "connectSpotify: " + success);
            }
        });
    }

}