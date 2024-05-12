package org.springblade.core.secure.annotation;

import java.lang.annotation.*;

/**
 * APP登录效验
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface AppLogin {
}
