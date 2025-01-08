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

import androidx.annotation.Nullable;

public class EventOrder {
    public static String orderToString(@Nullable Order order) {
        if (order == null) {
            return null;
        }

        switch (order) {
            case ID_ASC:
                return "id_asc";
            case ID_DESC:
                return "id_desc";
            case TIMESTAMP_ASC:
                return "timestamp_asc";
            case TIMESTAMP_DESC:
                return "timestamp_desc";
            default:
                throw new RuntimeException("Not implemented for " + order);
        }
    }

    public static Order orderFromString(@Nullable String order) {
        if (order == null) {
            return null;
        }

        switch (order) {
            case "id_asc":
                return Order.ID_ASC;
            case "id_desc":
                return Order.ID_DESC;
            case "timestamp_asc":
                return Order.TIMESTAMP_ASC;
            case "timestamp_desc":
                return Order.TIMESTAMP_DESC;
            default:
                return Order.ID_ASC;
//                throw new RuntimeException("Not implemented for " + order);
        }
    }

    public enum Order {
        ID_ASC,
        ID_DESC,
        TIMESTAMP_ASC,
        TIMESTAMP_DESC,
    }
}
