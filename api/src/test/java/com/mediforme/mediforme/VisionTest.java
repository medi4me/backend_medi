package com.mediforme.mediforme;

import com.google.cloud.vision.v1.*;

import java.util.ArrayList;
import java.util.List;

public class VisionTest {

    public String testGoogleVisionAPI() {
        try {
            // ImageAnnotatorClient 생성
            ImageAnnotatorClient vision = ImageAnnotatorClient.create();

            // 테스트용 이미지 파일 경로 또는 URL 설정
            String imageUrl = "http://connectdi.com/design/img/drug/147426411393800053.jpg"; // 테스트할 이미지 경로

            ImageSource imgSource = ImageSource.newBuilder().setImageUri(imageUrl).build();
            Image img = Image.newBuilder().setSource(imgSource).build();
            Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
            AnnotateImageRequest request =
                    AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
            List<AnnotateImageRequest> requests = new ArrayList<>();
            requests.add(request);

            // API 호출
            List<AnnotateImageResponse> responses = vision.batchAnnotateImages(requests).getResponsesList();

            // 결과 출력
            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    return "Error: " + res.getError().getMessage();
                }
                return res.getLabelAnnotationsList().toString();
            }

        } catch (Exception e) {
            return "Exception: " + e.getMessage();
        }

        return "No response";
    }
}
