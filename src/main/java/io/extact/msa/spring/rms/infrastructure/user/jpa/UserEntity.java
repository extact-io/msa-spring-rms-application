package io.extact.msa.spring.rms.infrastructure.user.jpa;

import static jakarta.persistence.AccessType.*;

import jakarta.persistence.Access;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

import io.extact.msa.spring.platform.fw.domain.type.UserType;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.jpa.TableEntity;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.User.UserCreatable;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Access(FIELD)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class UserEntity implements TableEntity<User>, UserCreatable {

    @Id
    private Integer id;
    private String loginId;
    private String password;
    private String userName;
    private String phoneNumber;
    private String contact;
    @Enumerated(EnumType.STRING)
    private UserType userType;

    public static UserEntity from(User model) {
        return new UserEntity(
                model.getId().id(),
                model.getLoginId(),
                model.getPassword(),
                model.getProfile().getUserName(),
                model.getProfile().getPhoneNumber(),
                model.getProfile().getContact(),
                model.getUserType());
    }

    @Override
    public User toModel() {
        return newInstance(
                new UserId(id),
                loginId,
                password,
                userType,
                userName,
                phoneNumber,
                contact);
    }
}
