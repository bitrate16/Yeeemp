/**
 * Copyright 2024 pegasko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package art.pegasko.yeeemp.base;

public abstract class Wrapper {
    private static Wrapper instance;

    public static Wrapper instance() {
        return Wrapper.instance;
    }

    public static void setInstance(Wrapper instance) {
        Wrapper.instance = instance;
    }

    public static QueueMaker getQueueMaker() {
        return Wrapper.instance().queueMaker();
    }

    public static EventMaker getEventMaker() {
        return Wrapper.instance().eventMaker();
    }

    public static TagMaker getTagMaker() {
        return Wrapper.instance().tagMaker();
    }

    public abstract QueueMaker queueMaker();
    public abstract EventMaker eventMaker();
    public abstract TagMaker tagMaker();
}
