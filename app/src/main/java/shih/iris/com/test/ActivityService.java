package shih.iris.com.test;

import android.app.Activity;
import android.content.res.AssetManager;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class ActivityService extends AppCompatActivity {

    private static final String LOG_TAG = "Test";

    String appFolderPath;
    String systemPath;

    // link jni library
    static {
        System.loadLibrary("jnilibsvm");
    }

    // connect the native functions
    private native void jniSvmTrain(String cmd);
    private native void jniSvmPredict(String cmd);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        systemPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SensorData.txt";
        appFolderPath = systemPath+"libsvm/";


        // 1. assign model/output paths
        String dataTrainPath = appFolderPath+"heart_scale ";
        String dataPredictPath = appFolderPath+"heart_scale ";
        String modelPath = appFolderPath+"model ";
        String outputPath = appFolderPath+"predict ";

        // 2. use the user's x,,y,z acceleration as features
        // dataTrainPath = data(1:end/2,4:6);
        // modelPath = data(1:end/2,2);
        // dataPredictPath = data(end/2 + 1:end,4:6);
        // outputPath = data(end/2 + 1:end,2);

        // 3. make SVM train by using kernel_type:radial basis function
        String svmTrainOptions = "-t 2 ";
        jniSvmTrain(svmTrainOptions+dataTrainPath+modelPath);

        // 4. make SVM predict
        jniSvmPredict(dataPredictPath+modelPath+outputPath);
    }


}