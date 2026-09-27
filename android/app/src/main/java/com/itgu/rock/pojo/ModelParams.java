package com.itgu.rock.pojo;

public class ModelParams {
    private double learningRate;
    private int epochs;
    private int batchSize;

    public ModelParams() {}

    public ModelParams(double learningRate, int batchSize,int epochs) {
        this.learningRate = learningRate;
        this.batchSize = batchSize;
        this.epochs = epochs;

    }

    // getter 和 setter
    public double getLearningRate() { return learningRate; }
    public void setLearningRate(double learningRate) { this.learningRate = learningRate; }
    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
    public int getEpochs() { return epochs; }
    public void setEpochs(int epochs) { this.epochs = epochs; }


}
