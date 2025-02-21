package com.mallangs.global.oauth2.dto;

import lombok.Getter;

@Getter
public class MemberOAuth2DTO {
    private Long memberId;
    private String userId;
    private String password;
    private String nickname;
    private String email;
    private String role;

    public void changeMemberId(Long memberId) {
        this.memberId = memberId;
    }
    public void changeUserId(String userId) {
        this.userId = userId;
    }
    public void changePassword(String password) {
        this.password = password;
    }
    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }
    public void changeEmail(String email) {
        this.email = email;
    }
    public void changeRole(String role) {
        this.role = role;
    }

}
