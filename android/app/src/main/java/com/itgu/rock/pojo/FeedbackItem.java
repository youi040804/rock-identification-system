package com.itgu.rock.pojo;
//反馈模型类
public class FeedbackItem {
    private Long id;
    private String rockName;
    private String imagePath;

    public FeedbackItem(Long id, String rockName, String imagePath) {
        this.id = id;
        this.rockName = rockName;
        this.imagePath = imagePath;
    }

    public Long getId() { return id; }
    public String getRockName() { return rockName; }
    public String getImagePath() { return imagePath; }
}
