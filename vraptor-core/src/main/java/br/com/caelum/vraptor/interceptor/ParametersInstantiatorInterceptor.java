/***
 * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.caelum.vraptor.interceptor;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.InterceptionException;
import br.com.caelum.vraptor.Lazy;
import br.com.caelum.vraptor.Validator;
import br.com.caelum.vraptor.core.InterceptorStack;
import br.com.caelum.vraptor.core.Localization;
import br.com.caelum.vraptor.core.MethodInfo;
import br.com.caelum.vraptor.http.MutableRequest;
import br.com.caelum.vraptor.http.ParametersProvider;
import br.com.caelum.vraptor.resource.ResourceMethod;
import br.com.caelum.vraptor.validator.Message;

/**
 * An interceptor which instantiates parameters and provide them to the stack.
 *
 * @author Guilherme Silveira
 */
@Lazy
public class ParametersInstantiatorInterceptor implements Interceptor {
    private final ParametersProvider provider;
    private final MethodInfo parameters;

    private static final Logger logger = LoggerFactory.getLogger(ParametersInstantiatorInterceptor.class);
    private final Validator validator;
    private final Localization localization;
	private final List<Message> errors = new ArrayList<Message>();
	private final HttpSession session;
	public static final String FLASH_PARAMETERS = "_vraptor_flash_parameters";
	private final MutableRequest request;

    public ParametersInstantiatorInterceptor(ParametersProvider provider, MethodInfo parameters,
            Validator validator, Localization localization, HttpSession session, MutableRequest request) {
        this.provider = provider;
        this.parameters = parameters;
        this.validator = validator;
        this.localization = localization;
		this.session = session;
		this.request = request;
    }

    public boolean accepts(ResourceMethod method) {
        return method.getMethod().getParameterTypes().length > 0;
    }

    @SuppressWarnings("unchecked")
	public void intercept(InterceptorStack stack, ResourceMethod method, Object resourceInstance) throws InterceptionException {
    	Enumeration<String> names = request.getParameterNames();
    	while (names.hasMoreElements()) {
			fixParameter(names.nextElement());
		}
        Object[] values = getParametersFor(method);

        validator.addAll(errors);

        if (logger.isDebugEnabled()) {
        	if (!errors.isEmpty()) {
        		logger.debug("There are conversion errors: {}", errors);
        	}
            logger.debug("Parameter values for {} are {}", method, values);
        }

        parameters.setParameters(values);
        stack.next(method, resourceInstance);
    }

	private void fixParameter(String name) {
		if (name.contains(".class.")) {
			throw new IllegalArgumentException("Bug Exploit Attempt with parameter: " + name + "!!!");
		}
		if (name.contains("[]")) {
			String[] values = request.getParameterValues(name);
			for (int i = 0; i < values.length; i++) {
				request.setParameter(name.replace("[]", "[" + i + "]"), values[i]);
			}
		}
	}

	private Object[] getParametersFor(ResourceMethod method) {
		Object[] args = (Object[]) session.getAttribute(ParametersInstantiatorInterceptor.FLASH_PARAMETERS);
		if (args == null) {
			return provider.getParametersFor(method, errors, localization.getBundle());
		}
		session.removeAttribute(ParametersInstantiatorInterceptor.FLASH_PARAMETERS);
		return args;
	}
}
