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

package de.hasait.clap.impl.usage;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import de.hasait.clap.CLAP;
import de.hasait.clap.impl.AbstractCLAPRelated;

public class CLAPUsagePrinter extends AbstractCLAPRelated {

    private final Deque<Category> currentCategoryDeque = new ArrayDeque<>();

    private final Map<String, Category> categoryMap = new HashMap<>();

    public CLAPUsagePrinter(CLAP clap) {
        super(clap);
    }

    public boolean pushNode(String categoryTitle, int categoryOrder, String prefix, String suffix, String separator, boolean decision, String distinctionKey) {
        Category category = handleCategory(categoryTitle, categoryOrder);
        if (category.pushNode(prefix, suffix, separator, decision, distinctionKey)) {
            currentCategoryDeque.addLast(category);
            return true;
        }
        return false;
    }

    public void popNode() {
        Category category = currentCategoryDeque.removeLast();
        category.popNode();
    }

    public void addEntry(String entryText) {
        Category category = handleCategory(null, 0);
        category.addEntry(entryText);
    }

    public void print(PrintStream printStream) {
        categoryMap.values().forEach(Category::beforePrint);

        int[] i = {0};
        categoryMap.values().stream() //
                   .sorted(Comparator.comparingInt(Category::getOrder)) //
                   .forEach(category -> {
                       printStream.print(category.getTitleForOutput(i[0] > 0));
                       printStream.print(": ");
                       StringBuilder stringBuilder = new StringBuilder();
                       category.getRootNode().print(stringBuilder);
                       printStream.print(stringBuilder.toString());
                       printStream.println();
                       i[0]++;
                   });
    }

    private Category handleCategory(String categoryTitle, int categoryOrder) {
        Category currentCategory = currentCategoryDeque.peekLast();
        Category category;
        if (categoryTitle == null) {
            if (currentCategory == null) {
                throw new RuntimeException("Root node must have an usage category");
            }
            category = currentCategory;
        } else {
            category = categoryMap.computeIfAbsent(categoryTitle, unusedKey -> new Category(clap, categoryTitle));
        }
        if (category.getOrder() == 0) {
            category.setOrder(categoryOrder);
        }
        if (category != currentCategory && currentCategory != null) {
            currentCategory.addCategoryRef(category);
        }
        return category;
    }

}
