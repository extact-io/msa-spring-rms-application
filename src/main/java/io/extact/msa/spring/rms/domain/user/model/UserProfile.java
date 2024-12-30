package io.extact.msa.spring.rms.domain.user.model;

import io.extact.msa.spring.platform.fw.domain.constraint.Contact;
import io.extact.msa.spring.platform.fw.domain.constraint.PhoneNumber;
import io.extact.msa.spring.platform.fw.domain.constraint.UserName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@ToString
@EqualsAndHashCode
public class UserProfile implements UserProfileReference {

    private @UserName String userName;
    private @PhoneNumber String phoneNumber;
    private @Contact String contact;

    public void editProfile(String userName, String phoneNumber, String contact) {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.contact = contact;
    }
}
