package com.itgu.rock.model;

public class RockProbability {
    private String name;       // 岩石名称
    private float probability; // 识别概率（0-100）

    // 构造方法
    public RockProbability(String name, float probability) {
        this.name = name;
        this.probability = probability;
    }

    // getter方法
    public String getName() {
        return name;
    }

    public float getProbability() {
        return probability;
    }
}
