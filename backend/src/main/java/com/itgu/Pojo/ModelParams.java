package com.itgu.Pojo;
//#############修改参数

public class ModelParams {
    private double learningRate;
    private int batchSize;
    private int epochs;

    // getter / setter
    public double getLearningRate() { return learningRate; }
    public void setLearningRate(double learningRate) { this.learningRate = learningRate; }


    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }


    public int getEpochs() { return epochs; }
    public void setEpochs(int epochs) { this.epochs = epochs; }
}
