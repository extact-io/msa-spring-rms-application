package io.extact.msa.spring.rms.infrastructure.framework;

import org.springframework.context.event.EventListener;

import io.extact.msa.spring.platform.core.auth.user.AuthUserId;
import io.extact.msa.spring.platform.fw.feature.auth.RdbAttributesProvider;
import io.extact.msa.spring.platform.fw.feature.auth.RmsLoginUserAttributes;
import io.extact.msa.spring.rms.domain.user.event.UserAddedEvent;
import lombok.RequiredArgsConstructor;

/**
 * ユーザー登録のドメインイベントに反応して、ログインユーザ拡張属性を
 * 自動で登録するハンドラ。
 * FW機能のログインユーザ拡張属性(..fw.feature)に対するアプリ固有処理と
 * なるため、infrastructure.frameworkパッケージに配置している
 */
@RequiredArgsConstructor
public class LoginUserAttributesAutoRegisterHandler {

    private final RdbAttributesProvider provider;

    @EventListener
    void handle(UserAddedEvent event) {
        int id = event.aadedUser().getId().id();
        RmsLoginUserAttributes attributes = new RmsLoginUserAttributes(
                new AuthUserId(id),
                "ID-%sの拡張属性1".formatted(id),
                "ID-%sの拡張属性2".formatted(id),
                "ID-%sの拡張属性3".formatted(id)
                );
        provider.register(attributes);
    }
}
