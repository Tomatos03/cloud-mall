package com.onlineshop.framework.utils.image;

import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/17
 */
public class ImageUtil {
    public static Image urlToImage(String url) {
        return new Image(url);
    }

    public static String getMainImageUrl(@NonNull String urls) {
        return getMainImageUrl(urls, ",");
    }

    public static String getMainImageUrl(@NonNull String urls, @NonNull String delimiter) {
        String[] urlArray = urls.split(delimiter);
        return urlArray.length > 0 ? urlArray[0] : "";
    }

    public static List<String> createImageUrlList(@NonNull String urls) {
        return createImageUrlList(urls, ",");
    }

    public static List<String> createImageUrlList(@NonNull String urls, @NonNull String delimiter) {
        return Arrays.stream(urls.split(delimiter))
                     .collect(Collectors.toList());
    }

    public static List<Image> createImageList(String urls) {
        return createImageList(urls, ",");
    }

    public static List<Image> createImageList(String urls, String delimiter) {
        return Arrays.stream(urls.split(delimiter))
                     .map(Image::new)
                     .collect(Collectors.toList());
    }

    public static String joinImageUrls(List<String> urlList, String delimiter) {
        return String.join(delimiter, urlList);
    }

    public  static String joinImageUrls(List<String> urlList) {
        return joinImageUrls(urlList, ",");
    }
}
