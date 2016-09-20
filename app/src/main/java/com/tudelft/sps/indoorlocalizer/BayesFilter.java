package com.tudelft.sps.indoorlocalizer;

import java.io.FileReader;
import java.util.Arrays;

/**
 * Created by Boyang on 5/20/2016.
 */
public class BayesFilter {

    //array contains all probabilities of classes
    private Double[] classes;
    //size of the array
    private int size;

    /*
     * Construct a BayesFilter class with certain size
     */
    public BayesFilter(int size){
        this.size = size;
        classes = new Double[size];
        Arrays.fill(classes,(1.0 / size));
    }

    /*
     * Update the probability array with the signal strength of a certain devices
     */
    public void updateProb(FileReader file, int signal){
        Double sum = 0.0;
        JTable jt = new JTable(file);
        Matrix prior_table = new Matrix(jt.getData());
        Double prob_rssi = p_rssi(signal,prior_table);
        if(prob_rssi != 0.0){
            for(int i = 0; i < size; i ++){
                Double prob_rssi_cell = p_rssi_cell(i,signal,prior_table);
                classes[i] = classes[i] * prob_rssi_cell / prob_rssi + 0.0005;
                sum += classes[i];
            }
            Normalize(sum);
        }
    }

    /*
     * Calculate the P(rssi|cell)
     */
    private Double p_rssi_cell(int c, int signal, Matrix prior_table){
        Double row_sum = prior_table.getRowSum(c);
        if(row_sum != 0){
            return prior_table.getData(c,signal + 100) / row_sum;
        }
        else{
            return 0.0;
        }
    }

    /*
     * Calculate the P(rssi)
     */
    private Double p_rssi(int signal, Matrix prior_table){
        return prior_table.getColumnSum(signal + 100) / prior_table.getSum();
    }

    /*
     * Normalize the probability array, make it sums to 1
     */
    private void Normalize(Double sum){
        if(sum != 0){
            for(int i = 0; i < size; i ++){
                classes[i] = classes[i] / sum;
            }
        }
    }

    /*
     * Get the predictions
     */
    public Double[] getClasses(){
        return this.classes;
    }
}
