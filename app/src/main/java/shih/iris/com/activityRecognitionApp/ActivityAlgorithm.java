package shih.iris.com.activityRecognitionApp;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


/**
 * Created by iriscshih on 12/2/15.
 */
public class ActivityAlgorithm {

    private static final String LOG_TAG = "ActivityRecognitionApp";
    String appFolderPath;
    String systemPath;
    // link jni library
    static {
        System.loadLibrary("jnilibsvm");
    }
    // connect the native functions
    private native void jniSvmTrain(String cmd);
    private native void jniSvmPredict(String cmd);


    private ArrayList<SensorData> sensorData;
    private Context activityContext;

    public ActivityAlgorithm(Activity act) {
        activityContext= act.getApplicationContext();
        this.sensorData = new ArrayList<SensorData>();
    }

    public void addData(SensorData data) {
        this.sensorData.add(data);
    }

    public void clearData() {
        this.sensorData.clear();
    }

    public boolean calculate() {

        for(SensorData dataItem:sensorData) {

            double accX = dataItem.getAccX();
            double accY = dataItem.getAccY();
            double accZ = dataItem.getAccZ();
            long timestamp = dataItem.getTimestamp();

            svmClassification();

        }

        // Return true if you are running, otherwise false
        return true;
    }

    public void svmClassification(){
        systemPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        appFolderPath = systemPath+"libsvm/";
        System.out.println("App folder path:" + appFolderPath);

        CreateAppFolderIfNeed();
        CopyAssetsDataIfNeed();

        // Assign model/output paths
        // Use user's x,,y,z acceleration as features
        String dataTrainPath = appFolderPath+"trainingData";
        String dataPredictPath = appFolderPath+"sensorData ";
        String modelPath = appFolderPath+"model ";
        String outputPath = appFolderPath+"predict ";

        // TODO:timestamps should be also part of the feature for calculating the variance/std of the x,y,z acceleration

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            try {
                // create a file in downloads directory
                String filename = appFolderPath+"sensorData";
                Log.d("Iris","creating filename: "+filename);
                new File(filename).createNewFile();
                FileOutputStream fos =
                        new FileOutputStream(filename);

                for(SensorData data:sensorData){
                    fos.write((sensorData.toString()+"\n").getBytes());
                    fos.flush();
                }

                fos.close();
                Log.v("MyApp","File has been written");
            } catch(Exception ex) {
                ex.printStackTrace();
                Log.v("MyApp","File didn't write");
            }
        }

        //SVM train by using kernel_type(-t):radial basis function(2)
        String svmTrainOptions = "-t 2 ";
        jniSvmTrain(svmTrainOptions+dataTrainPath+modelPath);

        //Make SVM predict
        jniSvmPredict(dataPredictPath+modelPath+outputPath);

    }


    public static class SensorData {
        private double accX;
        private double accY;
        private double accZ;
        private long timestamp;

        public SensorData(long timestamp, double accX, double accY, double accZ) {
            this.accX = accX;
            this.accY = accY;
            this.accZ = accZ;
            this.timestamp = timestamp;

        }
        public String toString(){
            return timestamp+", "+accX+", "+accY+", "+accZ;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public double getAccX() {
            return accX;
        }

        public void setAccX(double accX) {
            this.accX = accX;
        }

        public double getAccY() {
            return accY;
        }

        public void setAccY(double accY) {
            this.accY = accY;
        }

        public double getAccZ() {
            return accZ;
        }

        public void setAccZ(double accZ) {
            this.accZ = accZ;
        }
    }

    private void CreateAppFolderIfNeed(){
        // 1. create app folder if necessary
        File folder = new File(appFolderPath);

        if (!folder.exists()) {
            folder.mkdir();
            Log.d(LOG_TAG,"Appfolder is not existed, create one");
        } else {
            Log.w(LOG_TAG,"WARN: Appfolder has not been deleted");
        }
    }

    private void CopyAssetsDataIfNeed(){
        String assetsToCopy[] = {"trainingData", "model"};

        for(int i=0; i<assetsToCopy.length; i++){
            String from = assetsToCopy[i];
            String to = appFolderPath+from;

            // 1. check if file exist
            File file = new File(to);
            if(file.exists()){
                Log.d(LOG_TAG, "copyAssetsDataIfNeed: file exist, no need to copy:"+from);
            } else {
                // do copy
                boolean copyResult = copyAsset(activityContext.getAssets(), from, to);
                Log.d(LOG_TAG, "copyAssetsDataIfNeed: copy result = "+copyResult+" of file = "+from);
            }
        }
    }

    private boolean copyAsset(AssetManager assetManager, String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            out = new FileOutputStream(toPath);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "[ERROR]: copyAsset: unable to copy file = "+fromAssetPath);
            return false;
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

}
