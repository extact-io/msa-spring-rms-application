package io.extact.msa.spring.rms;

import static org.assertj.core.api.Assertions.*;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.metadata.BeanDescriptor;

import lombok.RequiredArgsConstructor;

/**
 * プロパティに対しチェックアノテーションが期待通り定義されているかを確認するクラス。
 * このクラスは以下の用途を想定している。<p>
 * ・モデルに対するバリデーション定義をBeanValidationが正しく認識しているかの確認(定義の検証)
 * ・意図通り定義できていることを確認できたら、後は代表的なパスを確認していけばOK
 * ・メタアノテーションが境界値等で正しく動作するかはメタアノテーションごとにテスト済み
 * ・よって、フィールドに対して正しいメタアノテーションが定義されそれをBeanValidationが認識している
 *   ところまで確認できれば、境界値等で正しく動作することは三段論法により担保されるため必要ない
 */
@RequiredArgsConstructor
public class PropertyAnnotationAsserter {

    private final Map<String, List<Class<? extends Annotation>>> propertyAnnotationMap;

    public static PropertyAnnotationAsserter asserterTo(BeanDescriptor bd) {

        // プロパティに定義されているチェックアノテーションを収集
        Map<String, List<Class<? extends Annotation>>> map = bd.getConstrainedProperties()
                .stream()
                .collect(Collectors.toMap(
                        pd -> pd.getPropertyName(),
                        pd -> pd.getConstraintDescriptors()
                                .stream()
                                .map(cd -> cd.getAnnotation().annotationType())
                                .collect(Collectors.toList())));

        return new PropertyAnnotationAsserter(map);
    }

    @SafeVarargs
    public final PropertyAnnotationAsserter verifyPropertyAnnotations(
            String propertyName,
            Class<? extends Annotation>... expected) {

        List<Class<? extends Annotation>> actual = propertyAnnotationMap.get(propertyName);
        assertThat(actual).containsExactlyInAnyOrder(expected);
        return this;
    }
}