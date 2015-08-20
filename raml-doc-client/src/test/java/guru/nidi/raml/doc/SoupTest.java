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
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SoupTest {
    @Test
    public void simple() {
        final Document html = Jsoup.parse("<table>" +
                "<tr><th>col1</th><th>col2</th></tr>" +
                "<tr><td>text</td><td></td></tr>" +
                "<tr><td><div>text</div>more</td><td></td></tr>" +
                "<tr><td colspan='2'><div>text</div></td></tr>" +
                "</table>");
        final Elements tables = html.select("table");
        for (final Element table : tables) {
            final Elements trs = table.getElementsByTag("tr");
            final List<Integer[]> widthsPerRow = new ArrayList<>();
            for (final Element tr : trs) {
                final Elements tds = tr.getElementsByTag("td");
                int col = 0;
                final Integer[] widths = new Integer[20];
                for (final Element td : tds) {
                    final String text = td.text();
                    widths[col] = text.length();
                    final int colspan = td.attr("colspan").length() == 0 ? 1 : Integer.parseInt(td.attr("colspan"));
                    col += colspan;
                }
                widthsPerRow.add(widths);
            }
            final Integer[] maxWidths = new Integer[20];
            for (final Integer[] widths : widthsPerRow) {
                for (int i = 0; i < widths.length; i++) {
                    if (widths[i] != null && (maxWidths[i] == null || widths[i] > maxWidths[i])) {
                        maxWidths[i] = widths[i];
                    }
                }
            }
            final Element tr = table.getElementsByTag("tr").first();
            final Elements ths = tr.getElementsByTag("th");
            int col = 0;
            for (Element th : ths) {
                if (maxWidths[col] != null && maxWidths[col] == 0) {
                    th.addClass("empty");
                }
                col++;
            }
            System.out.println(html.toString());
        }
    }
}