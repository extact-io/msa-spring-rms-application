package io.extact.msa.spring.rms.domain.user.model;

import io.extact.msa.spring.platform.fw.domain.model.ValueModel;
import io.extact.msa.spring.rms.domain.user.constraints.Contact;
import io.extact.msa.spring.rms.domain.user.constraints.PhoneNumber;
import io.extact.msa.spring.rms.domain.user.constraints.UserName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Value
public class UserProfile implements ValueModel {

    @UserName
    private String userName;
    @PhoneNumber
    private String phoneNumber;
    @Contact
    private String contact;
}
