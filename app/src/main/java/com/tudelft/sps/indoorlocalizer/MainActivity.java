package com.tudelft.sps.indoorlocalizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.tools.data.ARFFHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.tudelft.sps.indoorlocalizer.ArrayListFunction.getRange;
import static com.tudelft.sps.indoorlocalizer.ArrayListFunction.getSTD;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    //Views
    private Button buttonTestMode;
    private static DrawAMap viewCanvas;
    private Button LMbutton;

    //Interface
    private WifiManager wifiManager;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magneticfield;
    private WifiReceiver receiver;

    //Database
    private String path;

    //Classifier
    private Classifier knn;
    private Exclusion exc;
    private DecisionMaker dm;

    //Flags
    private boolean walking;
    private int last_location;
    private int orientation;
    private int iterations;

    //Temp Data
    private float aX = 0;
    private float aY = 0;
    private float aZ = 0;
    private ArrayList<Double> temp_aXs;
    private ArrayList<Double> temp_aYs;
    private ArrayList<Double> temp_aZs;
    private float[] aValue = new float[3];
    private float[] mValue = new float[3];
    private double last;

    //Number Format
    NumberFormat nf;

    private HashMap<String,BayesFilter> prob_map = new HashMap<String, BayesFilter>();

    public class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            iterations ++;
            if(iterations <= 15){
                Toast.makeText(context,"Iteration: " + iterations,Toast.LENGTH_SHORT).show();
//            if(walking) {
//                prob_map = new HashMap<String, BayesFilter>();
//            }
                List<ScanResult> scanResults = wifiManager.getScanResults();
                Double[] multiplier = exc.getMultiplier(scanResults);
                boolean b = false;
                BayesFilter bf = new BayesFilter(18);
                for (ScanResult scanResult : scanResults) {
                    String mac = scanResult.BSSID;
                    String mac_path = path + "/_" + mac.replace(":","_") + ".csv";
                    try{
                        FileReader file = new FileReader(mac_path);
                        b = true;
//                    if(prob_map.containsKey("1")){
//                        bf = prob_map.get("1");
//                    }
//                    else{
//                        bf = new BayesFilter(18);
//                    }
                        int strength = scanResult.level;
                        bf.updateProb(file, strength);
                    }
                    catch (Exception e){
                        continue;
                    }
                }
                prob_map.put("" + (iterations % 10),bf);
                Double[] classes = bf.getClasses();
                String text = "";
                int cell = 1;
                for(Double prob : classes){
                    text += cell + ":" + nf.format(prob) + " ";
                    cell ++;
                    if( cell % 2 !=  0){
                        text += "\r\n";
                    }
                }
                viewCanvas.setText(text);
                dm.combineStrategy(prob_map, walking, last_location,orientation,multiplier);
                //last_location = dm.getLocation();
                //viewCanvas.setText(b + " " + exc.showMultiplier(multiplier));
                if(b)
                    last_location = dm.getLocation();
                    //viewCanvas.setPos(last_location+1);
                else {
                    iterations = 15;
                    Toast.makeText(context,"Done",Toast.LENGTH_SHORT).show();
                    last_location = 17;
                    LMbutton.setClickable(true);
                }
                viewCanvas.setPos(last_location+1);
                if(iterations == 10){
                    Toast.makeText(context,"Done",Toast.LENGTH_SHORT).show();
                    LMbutton.setClickable(true);
                }
                wifiManager.startScan();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonTestMode = (Button) findViewById(R.id.buttonTestMode);
        LMbutton = (Button) findViewById(R.id.LMbutton);
        viewCanvas = (DrawAMap) findViewById(R.id.viewCanvas);

        viewCanvas.toggleTestMode();
        //Initialize
        receiver = new WifiReceiver();
        temp_aXs = new ArrayList<Double>();
        temp_aYs = new ArrayList<Double>();
        temp_aZs = new ArrayList<Double>();
        viewCanvas.setPos(last_location);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        //sensor initialize
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometer = sensorManager
                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            // No accelerometer!
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null){
            magneticfield = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            sensorManager.registerListener(this, magneticfield,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            // No accelerometer!
        }

        exc = new Exclusion();
        dm = new DecisionMaker();
        orientation = 0;
        iterations = 0;
        nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);

        path = Environment.getExternalStoragePublicDirectory("Download").getAbsolutePath();

        try{
            int[] data = {
                    R.raw._00_17_0f_83_50_bf,
                    R.raw._1c_aa_07_6e_31_a0,
                    R.raw._1c_aa_07_6f_28_50,
                    R.raw._1c_aa_07_6f_28_5f,
                    R.raw._1c_aa_07_7b_28_00,
                    R.raw._1c_aa_07_7b_37_00,
                    R.raw._1c_aa_07_7b_37_0f,
                    R.raw._1c_aa_07_7b_37_d0,
                    R.raw._1c_aa_07_7b_37_df,
                    R.raw._1c_aa_07_7b_39_10,
                    R.raw._1c_aa_07_7b_39_1f,
                    R.raw._1c_aa_07_b0_74_c0,
                    R.raw._1c_aa_07_b0_74_cf,
                    R.raw._1c_aa_07_b0_7a_b0,
                    R.raw._1c_aa_07_b0_7a_bf,
                    R.raw._1c_aa_07_b0_7c_00,
                    R.raw._1c_aa_07_b0_7c_0f,
                    R.raw._1c_aa_07_b0_7d_50,
                    R.raw._1c_aa_07_b0_7d_5f,
                    R.raw._1c_aa_07_b0_80_c0,
                    R.raw._1c_aa_07_b0_80_cf,
                    R.raw._24_01_c7_29_99_40,
                    R.raw._24_01_c7_76_2a_30,
                    R.raw._24_01_c7_76_2a_3f,
                    R.raw._54_4a_00_66_1c_b0,
                    R.raw._b8_be_bf_b7_7c_40,
                    R.raw._d8_24_bd_4f_cf_10,
                    R.raw._d8_24_bd_4f_cf_1f,
                    R.raw._d8_24_bd_4f_d8_e0,
                    R.raw._e8_ba_70_e7_34_70,
                    R.raw.data,
                    R.raw.exclusion
            };
            String[] data_name = {
                    "_00_17_0f_83_50_bf.csv",
                    "_1c_aa_07_6e_31_a0.csv",
                    "_1c_aa_07_6f_28_50.csv",
                    "_1c_aa_07_6f_28_5f.csv",
                    "_1c_aa_07_7b_28_00.csv",
                    "_1c_aa_07_7b_37_00.csv",
                    "_1c_aa_07_7b_37_0f.csv",
                    "_1c_aa_07_7b_37_d0.csv",
                    "_1c_aa_07_7b_37_df.csv",
                    "_1c_aa_07_7b_39_10.csv",
                    "_1c_aa_07_7b_39_1f.csv",
                    "_1c_aa_07_b0_74_c0.csv",
                    "_1c_aa_07_b0_74_cf.csv",
                    "_1c_aa_07_b0_7a_b0.csv",
                    "_1c_aa_07_b0_7a_bf.csv",
                    "_1c_aa_07_b0_7c_00.csv",
                    "_1c_aa_07_b0_7c_0f.csv",
                    "_1c_aa_07_b0_7d_50.csv",
                    "_1c_aa_07_b0_7d_5f.csv",
                    "_1c_aa_07_b0_80_c0.csv",
                    "_1c_aa_07_b0_80_cf.csv",
                    "_24_01_c7_29_99_40.csv",
                    "_24_01_c7_76_2a_30.csv",
                    "_24_01_c7_76_2a_3f.csv",
                    "_54_4a_00_66_1c_b0.csv",
                    "_b8_be_bf_b7_7c_40.csv",
                    "_d8_24_bd_4f_cf_10.csv",
                    "_d8_24_bd_4f_cf_1f.csv",
                    "_d8_24_bd_4f_d8_e0.csv",
                    "_e8_ba_70_e7_34_70.csv",
                    "data.arff",
                    "exclusion.csv"
            };
            for(int i = 0 ; i < data.length;i ++) {
                copyFile(path, data_name[i], data[i]);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        try {
            Dataset data = ARFFHandler.loadARFF(new File(path, "data.arff"), 6);
            knn = new KNearestNeighbors(3);
            knn.buildClassifier(data);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        buttonTestMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewCanvas.toggleDisplayMode();
            }
        });

        LMbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                iterations = 0;
                prob_map = new HashMap<String, BayesFilter>();
                wifiManager.startScan();
                LMbutton.setClickable(false);
            }
        });

        //start receiver
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magneticfield, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // onPause() unregister the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        unregisterReceiver(receiver);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            aValue = event.values;

            // get the the x,y,z values of the accelerometer
            aX = event.values[0];
            aY = event.values[1];
            aZ = event.values[2];

            //prediction with KNN

            if (temp_aXs.size() < 20) {
                temp_aXs.add((double) aX);
                temp_aYs.add((double) aY);
                temp_aZs.add((double) aZ);
            } else {
                double[] temp = {getSTD(temp_aXs), getSTD(temp_aYs),
                        getSTD(temp_aZs), getRange(temp_aXs),
                        getRange(temp_aYs), getRange(temp_aZs)};
                temp_aXs.clear();
                temp_aYs.clear();
                temp_aZs.clear();
                Instance instance = new DenseInstance(temp);
                Object predict = knn.classify(instance);
                if (predict.toString().equals("1")) {
                    walking = true;
                } else {
                    walking = false;
                }
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mValue = event.values;
            this.orientation = this.calculateOrientation();
            viewCanvas.setAngle(this.orientation);
        }
    }

    private void copyFile(String fileDirPath, String fileName, int id) {
        String filePath = fileDirPath + "/" + fileName;
        try {
            File dir = new File(fileDirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(filePath);
            if (!file.exists()) {
                InputStream is = getResources().openRawResource(
                        id);
                FileOutputStream fs = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fs.write(buffer, 0, count);
                }
                fs.close();
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int calculateOrientation() {
        final float ALPHA = 0.8f;

        float[] values = new float[3];
        float[] R = new float[9];

        SensorManager.getRotationMatrix(R, null, aValue, mValue);
        SensorManager.getOrientation(R, values);


        double differ = values[0] - last;
        if(differ > Math.PI) {
            differ = differ - 2 * Math.PI;
        }
        else if(differ < Math.PI) {
            differ = differ + 2 * Math.PI;
        }
        last = last + ALPHA * differ;
        if(last> Math.PI)
            while(last>-1) {
                last = last - 2 * Math.PI;
            }
        if(last< Math.PI)
            while(last<1) {
                last = last + 2 * Math.PI;
            }

        //viewCanvas.setText(""+((int)(-Math.toDegrees(values[0])-20))/6*6+"\r\n"+values[0]);
        return ((int)(-Math.toDegrees(values[0])-20))/6*6;
    }
}
