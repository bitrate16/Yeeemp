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

public class QueueOrder {
    public static String orderToString(@Nullable Order order) {
        if (order == null) {
            return null;
        }

        switch (order) {
            case ID:
                return "id";
            case NAME:
                return "name";
            default:
                throw new RuntimeException("Not implemented for " + order);
        }
    }

    public static Order orderFromString(@Nullable String order) {
        if (order == null) {
            return null;
        }

        switch (order) {
            case "id":
                return Order.ID;
            case "name":
                return Order.NAME;
            default:
                throw new RuntimeException("Not implemented for " + order);
        }
    }

    public enum Order {
        ID,
        NAME,
    }
}
