package com.tudelft.sps.indoorlocalizer;

import android.net.wifi.ScanResult;
import android.os.Environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Boyang on 6/24/2016.
 */
public class Exclusion {

    private ArrayList<String> mac_list;

    public Exclusion(){
        mac_list = new ArrayList<String>();
//		mac_list.add("00:17:0f:83:50:bf");
//		mac_list.add("1c:aa:07:6e:31:a0");
        mac_list.add("1c:aa:07:6e:31:af");
//		mac_list.add("1c:aa:07:6f:28:50");
//      mac_list.add("1c:aa:07:6f:28:5f");
        mac_list.add("1c:aa:07:7b:28:00");
        mac_list.add("1c:aa:07:7b:28:0f");
//		mac_list.add("1c:aa:07:7b:37:00");
//		mac_list.add("1c:aa:07:7b:37:0f");
//		mac_list.add("1c:aa:07:7b:37:d0");
//		mac_list.add("1c:aa:07:7b:37:df");
//		mac_list.add("1c:aa:07:7b:39:10");
        mac_list.add("1c:aa:07:7b:39:1f");
//		mac_list.add("1c:aa:07:b0:74:c0");
        mac_list.add("1c:aa:07:b0:74:cf");
//		mac_list.add("1c:aa:07:b0:7a:b0");
        mac_list.add("1c:aa:07:b0:7a:bf");
//		mac_list.add("1c:aa:07:b0:7c:00");
        mac_list.add("1c:aa:07:b0:7c:0f");
//		mac_list.add("1c:aa:07:b0:7d:50");
        mac_list.add("1c:aa:07:b0:7d:5f");
//		mac_list.add("1c:aa:07:b0:80:c0");
        mac_list.add("1c:aa:07:b0:80:cf");
//		mac_list.add("24:01:c7:29:99:40");
//		mac_list.add("24:01:c7:76:2a:30");
//		mac_list.add("24:01:c7:76:2a:3f");
//		mac_list.add("54:4a:00:66:1c:b0");
//		mac_list.add("b8:be:bf:b7:7c:40");
//		mac_list.add("d8:24:bd:4f:cf:10");
//		mac_list.add("d8:24:bd:4f:cf:1f");
//		mac_list.add("d8:24:bd:4f:d8:e0");
//		mac_list.add("e8:ba:70:e7:34:70");
    }

    public ArrayList<String> notReceivedMacs(List<ScanResult> scanResults){
        ArrayList<String> not_received_macs = (ArrayList<String>) this.mac_list.clone();
        for(ScanResult scanResult : scanResults){
            if(mac_list.contains(scanResult.BSSID)){
                not_received_macs.remove(scanResult.BSSID);
            }
        }
        return not_received_macs;
    }

    public Double[] getMultiplier(List<ScanResult> scanResults){
        Double[] multiplier = new Double[18];
        Arrays.fill(multiplier,1.0);
        String path = Environment.getExternalStoragePublicDirectory("Download").getAbsolutePath() + "/Exclusion.csv";
        ArrayList<String> not_received_macs = this.notReceivedMacs(scanResults);
        JTable jt = new JTable(path);
        Matrix m = new Matrix(jt.getData());
        for(String mac : not_received_macs){
            int index = mac_list.indexOf(mac);
            Double[] row = m.getMatrix()[index];
            multiplier = this.arrayMultiply(multiplier,row);
        }
        return multiplier;
    }

    private Double[] arrayMultiply(Double[] array_1,Double[] array_2){
        Double[] output = array_1.clone();
        for(int i = 0; i < array_1.length; i ++){
            output[i] = array_1[i] * array_2[i];
        }
        return output;
    }

    public String showMultiplier(Double[] multiplier){
        String output = "";
        for(Double d : multiplier){
            output += d.intValue() + ",";
        }
        return output;
    }
}
