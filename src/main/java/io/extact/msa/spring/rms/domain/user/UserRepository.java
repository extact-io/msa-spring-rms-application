package io.extact.msa.spring.rms.domain.user;

import java.util.Optional;

import io.extact.msa.spring.platform.fw.domain.repository.GenericRepository;
import io.extact.msa.spring.platform.fw.domain.service.DuplicationDataFinder;
import io.extact.msa.spring.platform.fw.domain.service.IdentityGenerator;
import io.extact.msa.spring.rms.domain.user.model.User;

public interface UserRepository extends GenericRepository<User>, DuplicationDataFinder<User>, IdentityGenerator {

    /**
     * ログインIDとパスワードに一致するユーザを取得。
     *
     * @param loginId ログインID
     * @param password パスワード
     * @return 該当ユーザ。該当なしはnull
     */
    Optional<User> findByLoginIdAndPassword(String loginId, String password);
}