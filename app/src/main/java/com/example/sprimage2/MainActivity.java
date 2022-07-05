package com.example.sprimage2;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sprimage2.databinding.ActivityMainBinding;

import org.opencv.core.Mat;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'sprimage2' library on application startup.
    static {
        System.loadLibrary("sprimage2");
    }

    private ActivityMainBinding binding;
    private final String TOP_TAG = "SPR-Image_2";
    private Uri referenceImageUri = null;
    private Uri currentImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Example of a call to a native method
        TextView tv = binding.sampleText;
        tv.setText(stringFromJNI());
    }

    ActivityResultLauncher<Intent> selectReferenceImageUri = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent data = result.getData();
                referenceImageUri = Objects.requireNonNull(data).getData();
                if (referenceImageUri != null) {
                    ImageView refView = (ImageView) findViewById(R.id.ReferenceView);
                    refView.setImageURI(referenceImageUri);
                }
            }
    );

    ActivityResultLauncher<String> selectCurrentImageUri = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    currentImageUri = uri;
                    if (currentImageUri != null) {
                        ImageView curView = (ImageView) findViewById(R.id.CurrentView);
                        curView.setImageURI(currentImageUri);
                    }
                }
            });

    public void selectReference(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        selectReferenceImageUri.launch(intent);
    }

    public void selectCurrent(View view) {
        selectCurrentImageUri.launch("image/*");
    }

    public void doComputation(View view) {
        if (currentImageUri != null && referenceImageUri != null){
            startComputation();
        } else if (currentImageUri == null) {
            selectCurrent(view);
        } else if (referenceImageUri == null) {
            selectReference(view);
        }
    }

    public void startComputation(){

    }

    private native double computeFromJni(long mat_Addr_cur, long mat_Addr_ref);

    /**
     * A native method that is implemented by the 'sprimage2' native library,
     * which is packaged with this application.
     */
//    public native String stringFromJNI();
}