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

 package art.pegasko.yeeemp.okand.base;

public abstract class Potato {
    private static Potato instance;

    public static Potato getInstance() {
        return Potato.instance;
    }

    public void setInstance(Potato instance) {
        Potato.instance = instance;
    }

    public abstract Event getById(int id);
    public abstract void save(Event container);
    public abstract void delete(Event container);
}
