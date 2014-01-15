package org.realityforge.replicant.example.server.service.tyrelll.replicate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.interceptor.InterceptorBinding;

@Inherited
@Target( { ElementType.TYPE, ElementType.METHOD } )
@Retention( RetentionPolicy.RUNTIME )
@InterceptorBinding
public @interface Replicate
{
}
