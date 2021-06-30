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

package de.hasait.clap.impl.help;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.hasait.clap.CLAP;
import de.hasait.clap.impl.AbstractCLAPRelated;

public class CLAPHelpPrinter extends AbstractCLAPRelated {

    private final Deque<Category> currentCategoryDeque = new ArrayDeque<>();

    private final Map<String, Category> categoryMap = new HashMap<>();

    public CLAPHelpPrinter(CLAP clap) {
        super(clap);
    }

    public void pushCurrentCategory(String title, int order) {
        Category category = handleCategory(title, order);
        currentCategoryDeque.addLast(category);
    }

    public void popCurrentCategory() {
        currentCategoryDeque.removeLast();
    }

    public void addEntry(String categoryTitle, int categoryOrder, String entryTitle, String entryDescription, int entryOrder) {
        Category category = handleCategory(categoryTitle, categoryOrder);
        category.addEntry(entryTitle, entryDescription, entryOrder);
    }

    public void print(PrintStream printStream) {
        int entryTitleMaxLength = categoryMap.values().stream() //
                                             .map(Category::getEntries).flatMap(Collection::stream) //
                                             .map(Entry::getTitle).mapToInt(String::length) //
                                             .max().orElse(0);
        int entryTitleMaxLengthWithSpace = entryTitleMaxLength + 4;

        categoryMap.values().stream() //
                   .sorted(Comparator.comparingInt(Category::getOrder).thenComparing(Category::getTitle)) //
                   .forEach(category -> {
                       printStream.println();
                       printStream.println(nls(category.getTitle()));

                       category.getEntries().stream() //
                               .sorted(Comparator.comparingInt(Entry::getOrder).thenComparing(Entry::getTitle)) //
                               .distinct() //
                               .forEach(entry -> {
                                   printStream.println();
                                   printStream.print("  ");
                                   printStream.print(StringUtils.rightPad(entry.getTitle(), entryTitleMaxLengthWithSpace));
                                   final String description = entry.getDescription();
                                   if (description != null) {
                                       printStream.println(nls(description));
                                   } else {
                                       printStream.println();
                                   }
                               });
                   });
    }

    private Category handleCategory(String title, int order) {
        Category current = currentCategoryDeque.peekLast();
        Category result;
        if (title == null) {
            if (current == null) {
                throw new RuntimeException("CLAP must have set a help category");
            }
            result = current;
        } else {
            result = categoryMap.computeIfAbsent(title, unusedKey -> new Category(title));
        }
        if (result.getOrder() == 0) {
            result.setOrder(order);
        }
        return result;
    }

}
