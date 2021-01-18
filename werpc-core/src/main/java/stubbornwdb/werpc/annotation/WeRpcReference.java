package stubbornwdb.werpc.annotation;


import java.lang.annotation.*;

/**
 * RPC reference annotation, autowire the service implementation class
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface WeRpcReference {

    /**
     * Service version, default value is empty string
     */
    String version() default "";

    /**
     * Service group, default value is empty string
     */
    String group() default "";

}
