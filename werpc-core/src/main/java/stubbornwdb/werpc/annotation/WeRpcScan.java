package stubbornwdb.werpc.annotation;

import org.springframework.context.annotation.Import;
import stubbornwdb.werpc.spring.CustomScannerRegistrar;

import java.lang.annotation.*;

/**
 * scan custom annotations
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Import(CustomScannerRegistrar.class)
@Documented
public @interface WeRpcScan {

    String[] basePackage();

}
