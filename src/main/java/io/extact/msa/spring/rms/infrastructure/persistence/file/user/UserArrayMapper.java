package io.extact.msa.spring.rms.infrastructure.persistence.file.user;

import io.extact.msa.spring.platform.fw.domain.model.ModelPropertySupportFactory;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.ModelArrayMapper;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.User.UserCreatable;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.domain.user.model.UserType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserArrayMapper implements ModelArrayMapper<User>, UserCreatable {

    private final ModelPropertySupportFactory modelSupportFactory;

    @Override
    public User toModel(String[] attributes) {

        int id = Integer.parseInt(attributes[0]);
        String loginId = attributes[1];
        String password = attributes[2];
        String userName = attributes[3];
        String phoneNumber = attributes[4];
        String contact = attributes[5];
        UserType userType = UserType.valueOf(attributes[6]);

        User user = newInstance(
                new UserId(id),
                loginId,
                password,
                userType,
                userName,
                phoneNumber,
                contact);
        user.configureSupport(modelSupportFactory);
        return user;
    }

    @Override
    public String[] toArray(User user) {

        String[] attributes = new String[7];

        attributes[0] = String.valueOf(user.getId().id());
        attributes[1] = user.getLoginId();
        attributes[2] = user.getPassword();
        attributes[3] = user.getProfile().getUserName();
        attributes[4] = user.getProfile().getPhoneNumber();
        attributes[5] = user.getProfile().getContact();
        attributes[6] = user.getUserType().name();

        return attributes;
    }
}
