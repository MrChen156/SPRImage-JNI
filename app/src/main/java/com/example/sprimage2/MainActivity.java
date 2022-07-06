package com.example.sprimage2;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sprimage2.databinding.ActivityMainBinding;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    // Used to load the 'sprimage2' library on application startup.
    static {
        System.loadLibrary("sprimage2");
    }

    private ActivityMainBinding binding;
    private final String TOP_TAG = "SPR-Image_2";
    private Uri referenceImageUri = null;
    private Uri currentImageUri = null;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TOP_TAG, "开干！");

        Button btnCurrent = (Button) findViewById(R.id.curBtn);
        Button btnReference = (Button) findViewById(R.id.refBtn);
        Button btnCompute = (Button) findViewById(R.id.comBtn);

//        binding = ActivityMainBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());

        Log.i(TOP_TAG, "监听buttons");
        btnCompute.setOnClickListener(this);
        btnCurrent.setOnClickListener(this);
        btnReference.setOnClickListener(this);
    }

    @Override
    public void onClick(View view){
        int id = view.getId();
        if (id == R.id.refBtn) {
            selectReference();
        } else if (id == R.id.curBtn) {
            selectCurrent();
        } else if (id == R.id.comBtn) {
            doComputation();
        }
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

    public void selectReference() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        selectReferenceImageUri.launch(intent);
    }

    public void selectCurrent() {
        selectCurrentImageUri.launch("image/*");
    }

    public void doComputation() {
        if (currentImageUri != null && referenceImageUri != null){
            startComputation();
        } else if (currentImageUri == null) {
            selectCurrent();
        } else if (referenceImageUri == null) {
            selectReference();
        }
    }

    public void startComputation(){
        // 判断大小在后面解决
        // 这里解决传参问题
        Bitmap bitmap;
        Mat cur = new Mat();
        Mat ref = new Mat();
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(currentImageUri));
            Utils.bitmapToMat(bitmap, cur);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(referenceImageUri));
            Utils.bitmapToMat(bitmap, ref);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 这一步耗时比较长，想办法移到后台：
        String result = computeFromJNI(cur.getNativeObjAddr(), ref.getNativeObjAddr());
        /*
        这一步要实现的效果的是把运算放到后台上，运算结果没出来时textView显示不变。
        当computeFromJNI完成运算之后，把结果显示到textView上面。
        不要用AsyncTask，用Executor解决这个问题，我自己写了一个MyApplication用来初始化线程池。
        */
        TextView textView = (TextView) findViewById(R.id.sample_text);
        textView.setText(result);
    }

    private String computeFromJNI(long mat_Addr_cur, long mat_Addr_ref) {
        Log.i(TOP_TAG, "Cur地址：" + mat_Addr_cur + "，Ref地址：" + mat_Addr_ref);
        String result = String.valueOf(computeFromJni(mat_Addr_cur, mat_Addr_ref));
        Log.i(TOP_TAG, "运算结果：" + result);
        return result;
    }

    private native double computeFromJni(long mat_Addr_cur, long mat_Addr_ref);

}