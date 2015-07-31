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

import org.junit.Test;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

/**
 *
 */
public class MarkdownTest {
    @Test
    public void table(){
        final PegDownProcessor processor = new PegDownProcessor(Extensions.TABLES);
        final String s = processor.markdownToHtml("```\n" +
                "      Status: 200 OK\n" +
                "      Link: <https://api.github.com/resource?page=2>; rel=\"next\",\n" +
                "            <https://api.github.com/resource?page=5>; rel=\"last\"\n" +
                "      X-RateLimit-Limit: 20\n" +
                "      X-RateLimit-Remaining: 19\n" +
                "\n" +
                "      {\n" +
                "        \"total_count\": 40,\n" +
                "        \"incomplete_results\": false,\n" +
                "        \"items\": [\n" +
                "          {\n" +
                "            \"id\": 3081286,\n" +
                "            \"name\": \"Tetris\",\n" +
                "            \"full_name\": \"dtrupenn/Tetris\",\n" +
                "            \"owner\": {\n" +
                "              \"login\": \"dtrupenn\",\n" +
                "              \"id\": 872147,\n" +
                "              \"avatar_url\": \"https://secure.gravatar.com/avatar/e7956084e75f239de85d3a31bc172ace?d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-user-420.png\",\n" +
                "              \"gravatar_id\": \"\",\n" +
                "              \"url\": \"https://api.github.com/users/dtrupenn\",\n" +
                "              \"received_events_url\": \"https://api.github.com/users/dtrupenn/received_events\",\n" +
                "              \"type\": \"User\"\n" +
                "            },\n" +
                "            \"private\": false,\n" +
                "            \"html_url\": \"https://github.com/dtrupenn/Tetris\",\n" +
                "            \"description\": \"A C implementation of Tetris using Pennsim through LC4\",\n" +
                "            \"fork\": false,\n" +
                "            \"url\": \"https://api.github.com/repos/dtrupenn/Tetris\",\n" +
                "            \"created_at\": \"2012-01-01T00:31:50Z\",\n" +
                "            \"updated_at\": \"2013-01-05T17:58:47Z\",\n" +
                "            \"pushed_at\": \"2012-01-01T00:37:02Z\",\n" +
                "            \"homepage\": \"\",\n" +
                "            \"size\": 524,\n" +
                "            \"stargazers_count\": 1,\n" +
                "            \"watchers_count\": 1,\n" +
                "            \"language\": \"Assembly\",\n" +
                "            \"forks_count\": 0,\n" +
                "            \"open_issues_count\": 0,\n" +
                "            \"master_branch\": \"master\",\n" +
                "            \"default_branch\": \"master\",\n" +
                "            \"score\": 10.309712\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "      ```");
        System.out.println(s);
    }
}
