package shih.iris.com.activityRecognitionApp;

import android.os.Environment;

import java.util.ArrayList;

/**
 * Created by pearl790131 on 12/2/15.
 */
public class ActivityAlgorithm {

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

    public ActivityAlgorithm() {
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

        // 1. assign model/output paths
        String dataTrainPath = appFolderPath+"traingingData";
        String dataPredictPath = appFolderPath+"sensorData ";
        String modelPath = appFolderPath+"model ";
        String outputPath = appFolderPath+"predict ";

        //TODO:use also timestamps as part of the feature to calculate the variance/std of the acceleration
        // 2. use the user's x,,y,z acceleration as features
//         dataTrainPath = data(1:end/2,4:6);
//         modelPath = data(1:end/2,2);
//         dataPredictPath = data(end/2 + 1:end,4:6);
//         outputPath = data(end/2 + 1:end,2);

        // 3. make SVM train by using kernel_type(-t):radial basis function(2)
        String svmTrainOptions = "-t 2 ";
        jniSvmTrain(svmTrainOptions+dataTrainPath+modelPath);

        // 4. make SVM predict
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

}
