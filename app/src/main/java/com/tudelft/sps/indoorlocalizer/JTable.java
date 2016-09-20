package com.tudelft.sps.indoorlocalizer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Created by Boyang on 5/20/2016.
 */
public class JTable {

    private String[][] csv_data;

    public JTable(String url){
        this.loadCSV(url);
    }

    public JTable(String[][] data){
        this.load(data);
    }

    public JTable(FileReader file){
        this.loadFile(file);
    }

    public void loadCSV(String url){

        String DELIMITER = ",";
        ArrayList<ArrayList<String>> data_list = new ArrayList<ArrayList<String>>();
        String line = null;

        try{
            //Create the file reader
            BufferedReader fileReader = new BufferedReader(new FileReader(url));

            //Read the file line by line
            while ((line = fileReader.readLine()) != null)
            {
                ArrayList<String> temp = new ArrayList<String>();
                //Get all tokens available in line
                String[] tokens = line.split(DELIMITER);
                for(String token : tokens){
                    temp.add(token);
                }
                data_list.add(temp);
            }

            //Close file reader
            fileReader.close();

            //dump data to csv_data
            this.list2Array(data_list);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadFile(FileReader file){

        String DELIMITER = ",";
        ArrayList<ArrayList<String>> data_list = new ArrayList<ArrayList<String>>();
        String line = null;

        try{
            //Create the file reader
            BufferedReader fileReader = new BufferedReader(file);

            //Read the file line by line
            while ((line = fileReader.readLine()) != null)
            {
                ArrayList<String> temp = new ArrayList<String>();
                //Get all tokens available in line
                String[] tokens = line.split(DELIMITER);
                for(String token : tokens){
                    temp.add(token);
                }
                data_list.add(temp);
            }

            //Close file reader
            fileReader.close();

            //dump data to csv_data
            this.list2Array(data_list);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void list2Array(ArrayList<ArrayList<String>> Data_list){
        int rows = Data_list.size();
        int columns = Data_list.get(0).size();
        this.csv_data = new String[rows][columns];
        for(int i = 0; i < rows; i ++){
            this.csv_data[i] = Data_list.get(i).toArray(new String[columns]);
        }
    }

    public void load(String[][] data){
        this.csv_data = data;
    }

    public String[][] getData(){
        return this.csv_data;
    }

    public String[] getRow(int index){
        return this.csv_data[index];
    }

    public String[] getColumn(int index){
        int rows = this.csv_data.length;
        String[] data = new String[rows];
        for(int i = 0; i < rows; i ++){
            data[i] = this.csv_data[i][index];
        }
        return data;
    }

    public String getData(int row, int column){
        return this.csv_data[row][column];
    }

    public int rows(){
        return this.csv_data.length;
    }

    public int columns(){
        return this.csv_data[0].length;
    }

}
