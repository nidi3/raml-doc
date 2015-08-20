/*
 * Copyright (C) 2015 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.raml.doc;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class HtmlOptimizer {
    private static final int MAX_WIDTH = 20;

    public void optimizeColumnWidths(File file) throws IOException {
        final Document html = Jsoup.parse(file, "utf-8");
        final Elements tables = html.select("table");
        for (final Element table : tables) {
            final List<Integer[]> widthsPerRow = calcWidths(table);
            final Integer[] maxWidths = calcMaxWidths(widthsPerRow);
            applyWidthStyle(table, maxWidths);
        }
        writeToFile(html, file);

    }

    private void writeToFile(Document html, File file) throws IOException {
        //new Document.OutputSettings().
        try (final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath()), "utf-8")) {
            out.write(html.toString());
        }
    }

    private void applyWidthStyle(Element table, Integer[] maxWidths) {
        final Element tr = table.getElementsByTag("tr").first();
        int col = 0;
        for (Element th : tr.getElementsByTag("th")) {
            if (maxWidths[col] != null) {
                if (maxWidths[col] <= MAX_WIDTH) {
                    th.attr("style", "width:" + maxWidths[col] + "em;");
                }
            }
            col++;
        }
    }

    private Integer[] calcMaxWidths(List<Integer[]> widthsPerRow) {
        final Integer[] maxWidths = new Integer[20];
        for (final Integer[] widths : widthsPerRow) {
            for (int i = 0; i < widths.length; i++) {
                if (widths[i] != null && (maxWidths[i] == null || widths[i] > maxWidths[i])) {
                    maxWidths[i] = widths[i];
                }
            }
        }
        return maxWidths;
    }

    private List<Integer[]> calcWidths(Element table) {
        final List<Integer[]> widthsPerRow = new ArrayList<>();
        for (final Element tr : table.getElementsByTag("tr")) {
            int col = 0;
            final Integer[] widths = new Integer[20];
            for (final Element td : tr.getElementsByTag("td")) {
                widths[col] = calcWidth(td);
                final int colspan = td.attr("colspan").length() == 0
                        ? 1
                        : Integer.parseInt(td.attr("colspan").trim());
                col += colspan;
            }
            widthsPerRow.add(widths);
        }
        return widthsPerRow;
    }

    private Integer calcWidth(Element td) {
        if (td.children().isEmpty()) {
            return td.text().length();
        }
        int max = 0;
        for (final Element ch : td.children()) {
            if (ch.nodeName().equals("p")) {
                max = Math.max(max, ch.text().length());
            } else {
                max = 1000;
            }
        }
        return max;
    }
}
