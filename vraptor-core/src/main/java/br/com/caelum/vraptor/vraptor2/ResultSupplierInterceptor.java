package br.com.caelum.vraptor.vraptor2;

import java.io.IOException;

import br.com.caelum.vraptor.InterceptionException;
import br.com.caelum.vraptor.Interceptor;
import br.com.caelum.vraptor.core.InterceptorStack;
import br.com.caelum.vraptor.ioc.Container;
import br.com.caelum.vraptor.resource.ResourceMethod;
import br.com.caelum.vraptor.vraptor2.outject.DefaultOutjecter;
import br.com.caelum.vraptor.vraptor2.outject.JsonOutjecter;

/**
 * Supplies the expected exporter (outjecter) for this specific request.<br>
 * Dependable on this specific request therefore being a request scoped
 * interceptor.
 * 
 * @author Guilherme Silveira
 */
public class ResultSupplierInterceptor implements Interceptor {

    private final Container container;
    private final ComponentInfoProvider info;

    public ResultSupplierInterceptor(Container container, ComponentInfoProvider info) {
        this.container = container;
        this.info = info;
    }

    public boolean accepts(ResourceMethod method) {
        return true;
    }

    public void intercept(InterceptorStack stack, ResourceMethod method, Object resourceInstance) throws IOException,
            InterceptionException {
        // simple version to do ajax parsing
        if (info.isAjax()) {
            container.register(JsonOutjecter.class);
        } else {
            container.register(DefaultOutjecter.class);
        }
        stack.next(method, resourceInstance);
    }

}
