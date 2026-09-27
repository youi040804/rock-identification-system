package com.itgu.rock.pojo;

public class FeedbackData {
    private String anonymousId;
    private String predictedLabel;
    private String correctLabel;
    private String description;
    private String imagePath;

    public FeedbackData(String anonymousId, String predictedLabel, String correctLabel, String description, String imagePath) {
        this.anonymousId = anonymousId;
        this.predictedLabel = predictedLabel;
        this.correctLabel = correctLabel;
        this.description = description;
        this.imagePath = imagePath;
    }

    // getter 和 setter
    public String getAnonymousId() {
        return anonymousId;
    }

    public void setAnonymousId(String anonymousId) {
        this.anonymousId = anonymousId;
    }

    public String getPredictedLabel() {
        return predictedLabel;
    }

    public void setPredictedLabel(String predictedLabel) {
        this.predictedLabel = predictedLabel;
    }

    public String getCorrectLabel() {
        return correctLabel;
    }

    public void setCorrectLabel(String correctLabel) {
        this.correctLabel = correctLabel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
