/**
 * Yeeemp - tag based event counter
 * Copyright (C) 2024-2025  pegasko
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 * Notwithstanding the freedoms granted by the AGPL 3.0 license, the following restrictions apply:
 *
 * Commercial usage of any kind of the project source code and/or project build artifacts (binaries, executables, packages, archives, libraries and/or any other artifacts) is strictly prohibited.
 *
 * Using the project source code and/or project build artifacts (binaries, executables, packages, archives, libraries and/or any other artifacts) for AI (Artificial Intelligence)/ML (Machine Learning)/NN (Neural Network) and/or any other kind of machine learning algorhitms and systems training is strictly prohibited.
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
