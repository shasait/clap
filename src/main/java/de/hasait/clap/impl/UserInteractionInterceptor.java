/*
 * Copyright (C) 2021 by Sebastian Hasait (sebastian at hasait dot de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.hasait.clap.impl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

import de.hasait.clap.CLAP;
import de.hasait.clap.CLAPOption;
import de.hasait.clap.CLAPUICallback;

/**
 * <p>A proxy factory for intercepting POJO getters with user interaction.</p>
 * <p>If the original getter returns <code>null</code> the {@link CLAPUICallback} is used to get the value from the user
 * and optionally storing it back in the POJO.</p>
 * Example:
 * <pre>
 *     class SomePojo {
 *         private String username;
 *         public String getUsername() { return username; }
 *         public void setUsername(String username) { this.username = username; }
 *     }
 *
 *     somewhereElse() {
 *         ...
 *         SomePojo original = new SomePojo();
 *         SomePojo proxy = new UserInteractionInterceptor&lt;&gt;(clap, SomePojo.class, uiCallback).intercept(original);
 *         proxy.getUsername(); // will trigger uiCallback
 *     }
 * </pre>
 *
 * @param <T>
 */
public class UserInteractionInterceptor<T> extends AbstractCLAPRelated {

    private final Class<T> clazz;
    private final CLAPUICallback uiCallback;

    private final Map<Method, String> readMethodToDescriptionMap = new HashMap<>();
    private final Map<Method, Method> readMethodToWriteMethodMap = new HashMap<>();
    private final Set<Method> passwordMethods = new HashSet<>();

    private final Class<T> proxyClass;

    private String prompt;
    private String cancelMessage;
    private boolean setAfterRead;

    public UserInteractionInterceptor(CLAP clap, Class<T> clazz, CLAPUICallback uiCallback) {
        super(clap);

        this.clazz = Objects.requireNonNull(clazz, "clazz must not be null");
        this.uiCallback = Objects.requireNonNull(uiCallback, "uiCallback must not be null");

        this.prompt = "Please enter {0}";
        this.cancelMessage = "User cancelled";
        this.setAfterRead = true;

        try {
            introspect();
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }

        this.proxyClass = createProxyClass();
    }

    public T intercept(T object) {
        MethodHandlerImpl methodHandler = new MethodHandlerImpl(object);
        T proxy;
        try {
            proxy = proxyClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        ((Proxy) proxy).setHandler(methodHandler);
        return proxy;
    }

    public String getPrompt() {
        return prompt;
    }

    /**
     * @param prompt The prompt for the user (plain or nls key).
     */
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getCancelMessage() {
        return cancelMessage;
    }

    /**
     * @param cancelMessage The prompt for the user (plain or nls key).
     */
    public void setCancelMessage(String cancelMessage) {
        this.cancelMessage = cancelMessage;
    }

    public boolean isSetAfterRead() {
        return setAfterRead;
    }

    /**
     * @param setAfterRead <code>true</code> if the property should be set to the value provided by the user.
     */
    public void setSetAfterRead(boolean setAfterRead) {
        this.setAfterRead = setAfterRead;
    }

    private void introspect() throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
        for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
            Method readMethod = propertyDescriptor.getReadMethod();
            Method writeMethod = propertyDescriptor.getWriteMethod();
            if (readMethod != null && writeMethod != null && propertyDescriptor.getPropertyType().equals(String.class)) {
                String description;
                CLAPOption clapOption = writeMethod.getAnnotation(CLAPOption.class);
                String descriptionNLSKey = clapOption == null ? null : clapOption.descriptionNLSKey();
                if (descriptionNLSKey != null && descriptionNLSKey.trim().length() != 0) {
                    description = descriptionNLSKey;
                } else {
                    description = propertyDescriptor.getDisplayName();
                }
                readMethodToDescriptionMap.put(readMethod, description);
                readMethodToWriteMethodMap.put(readMethod, writeMethod);
                if (clapOption != null && clapOption.password()) {
                    passwordMethods.add(readMethod);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Class<T> createProxyClass() {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(clazz);
        proxyFactory.setFilter(readMethodToDescriptionMap::containsKey);
        return proxyFactory.createClass();
    }

    private class MethodHandlerImpl implements MethodHandler {

        private final T object;

        private MethodHandlerImpl(T object) {
            this.object = Objects.requireNonNull(object, "object must not be null");
        }

        @Override
        public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable {
            String result = (String) method.invoke(object, args); // execute the original method.
            if (result == null) {
                boolean password = passwordMethods.contains(method);
                String description = readMethodToDescriptionMap.get(method);
                String promptNls = nls(prompt, description);
                String newResult = password ? uiCallback.readPassword(promptNls) : uiCallback.readLine(promptNls);
                if (newResult == null) {
                    throw new RuntimeException(nls(cancelMessage));
                }
                if (setAfterRead) {
                    readMethodToWriteMethodMap.get(method).invoke(object, newResult);
                }
                return newResult;
            }
            return result;
        }
    }

}
