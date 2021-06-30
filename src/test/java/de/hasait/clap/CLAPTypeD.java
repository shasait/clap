/*
 * Copyright (C) 2021 by Sebastian Hasait (sebastian at hasait dot de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.hasait.clap;

/**
 * Class used by {@link CLAPTest}.
 */
@CLAPKeywords({
        @CLAPKeyword("Hallo")
})
public class CLAPTypeD {

    private Boolean _boolean;

    private String _string;

    private int _int = 1000;

    public Boolean getBoolean() {
        return _boolean;
    }

    public int getInt() {
        return _int;
    }

    public String getString() {
        return _string;
    }

    @CLAPOption(shortKey = 'b', longKey = "dboolean")
    public void setBoolean(Boolean value) {
        _boolean = value;
    }

    @CLAPOption(longKey = "dint")
    public void setInt(int value) {
        _int = value;
    }

    @CLAPOption(longKey = "dstring", argCount = 1)
    public void setString(String string) {
        _string = string;
    }
}
