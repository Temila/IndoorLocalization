package com.tudelft.sps.indoorlocalizer;

import android.util.Log;

/**
 * Created by Boyang on 5/20/2016.
 */
public class Matrix {

    private Double[][] matrix;
    private Double matrix_sum;

    public Matrix(Double[][] array){
        int rows = array.length;
        int columns = array[0].length;
        matrix_sum = 0.0;
        this.matrix = array;
        for(int i = 0; i < rows; i ++) {
            for (int j = 0; j < columns; j++) {
                matrix_sum += array[i][j];
            }
        }
    }

    public Matrix(String[][] array){
        int rows = array.length;
        int columns = array[0].length;
        matrix_sum  = 0.0;
        this.matrix = new Double[rows][columns];
        try{
            for(int i = 0; i < rows; i ++){
                for(int j = 0; j < columns; j ++){
                    this.matrix[i][j] = Double.parseDouble(array[i][j]);
                    matrix_sum += Double.parseDouble(array[i][j]);
                }
            }
        }
        catch (Exception e){
            Log.e("Matrix","Unable to initialize Matrix, invalid symbol");
        }
    }

    public void Plus(Matrix M) {
        Double[][] input_matrix = M.getMatrix();
        int n = input_matrix.length;
        int m = input_matrix[0].length;
        if (n != matrix.length || m != matrix[0].length) {
            Log.e("Matrix","fail to add, dimension mismatch");
        } else {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    matrix[i][j] = matrix[i][j] + input_matrix[i][j];
                }
            }
        }
    }

    public void Minus(Matrix M) {
        Double[][] input_matrix = M.getMatrix();
        int n = input_matrix.length;
        int m = input_matrix[0].length;
        if (n != matrix.length || m != matrix[0].length) {
            Log.e("Matrix","fail to minus, dimension mismatch");
        } else {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    matrix[i][j] = matrix[i][j] - input_matrix[i][j];
                }
            }
        }
    }

    public void Times(Matrix M) {
        Double[][] input_matrix = M.getMatrix();
        int n = input_matrix.length;
        int m = input_matrix[0].length;
        Double[][] output_matrix = new Double[matrix.length][m];
        if (n != matrix[0].length) {
            Log.e("Matrix","fail to multiply, dimension mismatch");
        } else {
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < m; j++) {
                    output_matrix[i][j] = 0.0;
                    for (int k = 0; k < n; k++) {
                        System.out.println(output_matrix[i][j]);
                    }
                }
            }
        }
        matrix = output_matrix;
    }

    public Double[][] getMatrix(){
        return this.matrix;
    }

    public void setMatrix(Double[][] matrix){
        this.matrix = matrix;
    }

    public Double getSum(){
        return matrix_sum;
    }

    public Double getRowSum(int row){
        Double row_sum = 0.0;
        for(Double d : this.matrix[row]){
            row_sum += d;
        }
        return row_sum;
    }

    public Double getColumnSum(int column){
        int rows = this.matrix.length;
        Double column_sum = 0.0;
        for(int i = 0; i < rows; i ++){
            column_sum += this.matrix[i][column];
        }
        return column_sum;
    }

    public Double getData(int row, int column){
        return this.matrix[row][column];
    }

}
