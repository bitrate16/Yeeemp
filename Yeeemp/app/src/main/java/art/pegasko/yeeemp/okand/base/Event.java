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

import java.util.ArrayList;
import java.util.List;

import art.pegasko.yeeemp.base.Tag;

public class Event {
    private int id;
    private String comment;
    private List<Tag> tags;

    public Event() {
        this.id = -1;
        this.comment = null;
        this.tags = new ArrayList<Tag>();
    }

    void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static Event get(int id) {
        return Potato.getInstance().getById(id);
    }
}
