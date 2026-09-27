package com.itgu.Service;

public interface PyUtils {

    /**
     * 调用岩石识别模型。
     *
     * @param imagePath 待识别图片的绝对路径
     * @return Python 返回的 JSON 数组字符串
     */
    String searchRock(String imagePath);
}