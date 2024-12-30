package io.extact.msa.spring.rms.infrastructure.persistence.file.user;

import io.extact.msa.spring.platform.fw.domain.type.UserType;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.ModelArrayMapper;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.User.UserCreatable;
import io.extact.msa.spring.rms.domain.user.model.UserId;

public class UserArrayMapper implements ModelArrayMapper<User>, UserCreatable {

    public static final UserArrayMapper INSTANCE = new UserArrayMapper();

    @Override
    public User toModel(String[] attributes) {

        int id = Integer.parseInt(attributes[0]);
        String loginId = attributes[1];
        String password = attributes[2];
        String userName = attributes[3];
        String phoneNumber = attributes[4];
        String contact = attributes[5];
        UserType userType = UserType.valueOf(attributes[6]);

        return newInstance(
                new UserId(id),
                loginId,
                password,
                userType,
                userName,
                phoneNumber,
                contact);
    }

    @Override
    public String[] toArray(User user) {

        String[] attributes = new String[7];

        attributes[0] = String.valueOf(user.getId());
        attributes[1] = user.getLoginId();
        attributes[2] = user.getPassword();
        attributes[3] = user.getProfile().getUserName();
        attributes[4] = user.getProfile().getPhoneNumber();
        attributes[5] = user.getProfile().getContact();
        attributes[6] = user.getUserType().name();

        return attributes;
    }
}
