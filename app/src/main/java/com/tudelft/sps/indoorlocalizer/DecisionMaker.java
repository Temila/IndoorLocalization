package com.tudelft.sps.indoorlocalizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Boyang on 5/24/2016.
 */
public class DecisionMaker {

    private int location;
    private ArrayList<Integer> layout;

    public DecisionMaker(){
        location = 0;
        layout = new ArrayList<Integer>();
        layout.add(1);
        layout.add(2);
        layout.add(4);
        layout.add(5);
        layout.add(6);
        layout.add(8);
        layout.add(9);
        layout.add(10);
        layout.add(11);
        layout.add(13);
        layout.add(14);
        layout.add(15);
    }

    public Double[] combineStrategy(HashMap<String, BayesFilter> prob_map, boolean walking, int last_location, int orientation, Double[] multiplier){
        Set<String> keys = prob_map.keySet();
        if(keys.isEmpty()){
            BayesFilter bf = new BayesFilter(18);
            return bf.getClasses();
        }
        Double[] output = new Double[18];
        Arrays.fill(output,0.0);
        for(String key : keys){
            Double[] probs = prob_map.get(key).getClasses();
            output = arrayAddition(output,probs);
        }

        output = arrayMultiply(output,multiplier);

        Double weight = 1.05; // might need to change

        if( last_location != -1){
            output[last_location] *= weight;
            if(walking){
//                if(last_location != 1){
//                    output[layout.get(layout.indexOf(last_location) - 1)] *= weight;
//                }
//                if(last_location != 15){
//                    output[layout.get(layout.indexOf(last_location) + 1)] *= weight;
//                }
                try{
                    int index = layout.indexOf(last_location);
                    int direction  = this.getDirection(orientation);
                    if(direction == 1 || direction == -1){
                        output[layout.get(index + direction)] *= weight;
                    }
                    else{

                    }
                }
                catch (Exception e){

                }
            }
        }

        //normalize
        Double sum = 0.0;
        for(Double p : output){
            sum += p;
        }
        for(int i = 0; i < 18; i ++){
            output[i] = output[i] / sum;
        }
        this.location = getMaxIndex(output);
        return output;
    }

    public int getLocation(){
        return location;
    }

    private int getDirection(int orientation){
        if(orientation >= 45 && orientation < 135){
            return 1;
        }
        else if(orientation >= -135 && orientation < -45){
            return -1;
        }
        else if(orientation < 45 && orientation >=0 || orientation <= 0 && orientation >= -45){
            return 2;
        }
        else{
            return 0;
        }
    }

    private int getMaxIndex(Double[] arr){
        int maxIndex = 0;
        for(int i=0; i<arr.length; i++){
            if(arr[i] > arr[maxIndex]){
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private Double[] arrayAddition(Double[] array1, Double[] array2){
        Double[] output = new Double[array1.length];
        for(int i = 0; i < array1.length; i ++){
            System.out.print(array1[i] + " " + array2[i]);
            output[i] = array1[i] + array2[i];
        }
        return output;
    }

    private Double[] arrayMultiply(Double[] array_1,Double[] array_2){
        Double[] output = array_1.clone();
        for(int i = 0; i < array_1.length; i ++){
            output[i] = array_1[i] * array_2[i];
        }
        return output;
    }
}
