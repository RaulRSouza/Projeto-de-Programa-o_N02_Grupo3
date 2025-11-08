package br.com.unit.gerenciamentoAulas.ui.pages;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Helper class to access Spring ApplicationContext from non-Spring managed classes
 */
@Component
public class SpringContext {
    
    private static ApplicationContext context;
    
    public SpringContext(ApplicationContext applicationContext) {
        SpringContext.context = applicationContext;
    }
    
    public static ApplicationContext getSpringContext() {
        return context;
    }
    
    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }
}
