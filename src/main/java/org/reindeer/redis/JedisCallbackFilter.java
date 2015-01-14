/*
 * Copyright (c) 2014-2015 ,fangzy (zyuanf@gmail.com)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.reindeer.redis;

import org.springframework.cglib.proxy.CallbackFilter;

import java.lang.reflect.Method;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 2014/9/26.
 *
 * @author FZY
 */
public class JedisCallbackFilter implements CallbackFilter {

    private int id = ThreadLocalRandom.current().nextInt();

    @Override
    public int accept(Method method) {
        if ("finalize".equals(method.getName()) &&
                method.getParameterTypes().length == 0 &&
                method.getReturnType() == Void.TYPE) {
            return 0;
        }
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JedisCallbackFilter)) {
            return false;
        }

        JedisCallbackFilter that = (JedisCallbackFilter) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
