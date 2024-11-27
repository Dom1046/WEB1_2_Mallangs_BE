package com.mallangs.domain.article.entity;

import com.mallangs.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("place")
@SuperBuilder
public class PlaceArticle extends Article {

    @Column(length = 255)
    private String businessHours;

    @Column(length = 50)
    private String closeDays;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String website; //웹사이트링크

    // 추가 필드 (CSV 파일 기반)
    @Column(length = 20)
    private String category; // 카테고리3

    @Column(length = 255)
    private String address; // 주소

    @Column(length = 255)
    private String roadAddress; // 주소

    @Column
    private String basicInfo; // 기본정보필드

    @Column
    private Boolean hasParking; // 주차 가능 여부

    @Column
    private Boolean isPetFriendly; // 반려동물 동반 가능 여부


    @Override
    public void applyChanges(Article updatedArticle) {
        super.applyChanges(updatedArticle);

        PlaceArticle updatedPlaceArticle = (PlaceArticle) updatedArticle;

        if (updatedPlaceArticle.getBusinessHours() != null) {
            this.businessHours = updatedPlaceArticle.getBusinessHours();
        }
        if (updatedPlaceArticle.getCloseDays() != null) {
            this.closeDays = updatedPlaceArticle.getCloseDays();
        }
        if (updatedPlaceArticle.getWebsite() != null) {
            this.website = updatedPlaceArticle.getWebsite();
        }
        if (updatedPlaceArticle.getCategory() != null) {
            this.category = updatedPlaceArticle.getCategory();
        }
        if (updatedPlaceArticle.getAddress() != null) {
            this.address = updatedPlaceArticle.getAddress();
        }
        if (updatedPlaceArticle.getBasicInfo() != null) {
            this.basicInfo = updatedPlaceArticle.getBasicInfo();
        }
        if (updatedPlaceArticle.getHasParking() != null) {
            this.hasParking = updatedPlaceArticle.getHasParking();
        }
        if (updatedPlaceArticle.getIsPetFriendly() != null) {
            this.isPetFriendly = updatedPlaceArticle.getIsPetFriendly();
        }
    }
}

//    public static PlaceArticle createPlaceArticle(Member member, PlaceCreateRequest createRequest) {
//        return PlaceArticle.builder()
//                .businessHours(createRequest.getBusinessHours())
//                .closeDays(createRequest.getCloseDays())
//                .website(createRequest.getWebsite())
//                .build();
//    }