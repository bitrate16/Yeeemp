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
