package io.extact.msa.spring.rms.domain.user;

import java.util.Optional;

import io.extact.msa.spring.platform.fw.domain.repository.DuplicationDataFinder;
import io.extact.msa.spring.platform.fw.domain.repository.GenericRepository;
import io.extact.msa.spring.platform.fw.domain.repository.IdProvider;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.UserId;

public interface UserRepository extends GenericRepository<User>, DuplicationDataFinder<User>, IdProvider<UserId> {

    /**
     * ログインIDとパスワードに一致するユーザを取得。
     *
     * @param loginId ログインID
     * @param password パスワード
     * @return 該当ユーザ。該当なしはnull
     */
    Optional<User> findByLoginIdAndPassword(String loginId, String password);
}