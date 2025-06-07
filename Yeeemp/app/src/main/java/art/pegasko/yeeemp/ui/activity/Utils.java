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

package art.pegasko.yeeemp.ui.activity;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Utils {
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String formatTs(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(Utils.DATE_FORMAT);
        return sdf.format(timestamp);
    }

    public static String[] orderedDeduplicateIgnoreCaseAndTrim(String[] items) {
        Set<String> duplicates = new HashSet<String>();
        ArrayList<String> reverseOrderedItems = new ArrayList<String>();

        for (int index = items.length - 1; index >= 0; --index) {
            String item = items[index].toLowerCase().trim();
            if (!duplicates.contains(item) && !item.isEmpty()) {
                duplicates.add(item);
                reverseOrderedItems.add(item);
            }
        }

        String[] finalItems = new String[reverseOrderedItems.size()];
        for (int index = 0; index < finalItems.length; ++index) {
            finalItems[finalItems.length - index - 1] = reverseOrderedItems.get(index);
        }

        return finalItems;
    }

    public static int positiveHashCode16(Object o) {
        if (o == null)
            return 0;
        return Math.abs(o.hashCode()) & ((1 << 16) - 1);
    }

    public static void hapticTick(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Vibration muted, do nothing
            if (!view.isHapticFeedbackEnabled()) {
                return;
            }

            // Success only if haptics available and worked
            if (view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)) {
                return;
            }
        }

        // Fallback to basic vibration
        Vibrator vibrator = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            // TODO: Respect silent mode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(10, 50));
            } else {
                vibrator.vibrate(10);
            }
        }
    }
}
