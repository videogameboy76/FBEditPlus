/*
 * Frozen Bubble Level Editor Plus
 *
 * Edit and load custom level packs to Frozen Bubble for Android.
 *
 * Copyright (C) 2016 Rudo Halmi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
 */

package sk.halmi.fbeditplus.overview;

import android.app.Activity;

/**
 * <p>Define a placeholder class for the obsolete GetInfo library
 * originally developed by Rudolf Halmi for web server level pack
 * support.
 * <p>This class will allow the application to build to keep the
 * original source code in the project intact for reference purposes,
 * but any functionality that references this object should be excluded
 * from the application as it is only an empty stub.
 *
 * @author Eric Fortin
 *
 */
public class GetInfo {
  public static String getIdentifier(Activity activity) {
    return ("null");
  }
}
