package com.mallangs.domain.member.entity;

import com.mallangs.domain.chat.entity.ParticipatedRoom;
import com.mallangs.domain.member.entity.embadded.*;
import com.mallangs.domain.pet.entity.Pet;
import com.mallangs.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Entity
@ToString(exclude = "addresses")
@EqualsAndHashCode(of = "memberId", callSuper = false)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Embedded
    private UserId userId;

    @Embedded
    private Nickname nickname;

    @Embedded
    private Password password;

    @Embedded
    private Email email;

    //주소
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    //채팅방
    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ParticipatedRoom> participatedRooms = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MemberRole memberRole = MemberRole.ROLE_USER;

    @Column(name = "profile_image", columnDefinition = "TEXT")
    private String profileImage;

    @Column(name = "has_pet", nullable = false)
    private Boolean hasPet;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    //차단 만기 날짜
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    //차단 사유
    @Column(name = "reason_for_ban")
    private String reasonForBan;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    //Pet
    @JsonIgnore
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Pet> pets = new ArrayList<>();

    // 회원가입
    public Member(String userId, String nickname, Password password, String email, String profileImage, Boolean hasPet){
        this.userId = new UserId(userId);
        this.nickname = new Nickname(nickname);
        this.password = password;
        this.email = new Email(email);
        this.profileImage = profileImage;
        this.hasPet = hasPet;
    }

    //수정
    public void change(String nickname,String password, String email, PasswordEncoder passwordEncoder){
        this.nickname = new Nickname(nickname);
        this.password = new Password(password, passwordEncoder);
        this.email = new Email(email);
    }

    public void changeIsActive(Boolean isActive){
        this.isActive = isActive;
    }

    public void changePassword(Password password){
        this.password = password;
    }

    public void addAddress(Address address){
        this.addresses.add(address);
    }

    public void recordLoginTime(){
        this.lastLoginTime = LocalDateTime.now();
    }
    public void changeProfileImage(String profileImage){
        this.profileImage = profileImage;
    }
    public void addParticipatedRoom(ParticipatedRoom participatedRoom){
        this.participatedRooms.add(participatedRoom);
        participatedRoom.changeParticipant(this);
    }

    public void changeRole(MemberRole memberRole){
        this.memberRole = memberRole;
    }
}
