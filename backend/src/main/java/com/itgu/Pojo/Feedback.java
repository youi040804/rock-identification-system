package com.itgu.Pojo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;


@Entity
@Table(name = "feedbacks")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String rockName;
    private String imagePath;
    // 软删除标记（0=正常，1=已删除）
    private Integer isDeleted = 0;
    //补充至训练集标记
    private Integer inTrainingSet = 0; // 默认 0



    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRockName() {
        return rockName;
    }

    public void setRockName(String rockName) {
        this.rockName = rockName;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }


    public Integer getIsDeleted() {return isDeleted;}

    public void setIsDeleted(Integer isDeleted) {this.isDeleted = isDeleted;}

    public Integer getInTrainingSet() {return inTrainingSet;}

    public void setInTrainingSet(Integer inTrainingSet) {this.inTrainingSet = inTrainingSet;}


}
