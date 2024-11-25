package com.mallangs.domain.member.entity.embadded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.regex.Pattern;

@Embeddable
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Password {
    public static final String REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!#$%&'()*+,.:;<=>?@^_`{|}~])[A-Za-z\\d$@!%#*?&^]{8,20}$";
    public static final String ERR_MSG = "비밀번호는 8~20, 최소 하나의 영어소문자, 영어 대문자, 특수 문자, 숫자 이상 포함되어야 합니다.";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    @Column(name = "password", nullable = false)
    private String value;

    public Password(final String password, final PasswordEncoder passwordEncoder) {
        if (!PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException(ERR_MSG);
        }
        this.value = passwordEncoder.encode(password);
    }
}
