package guru.nidi.raml.doc;/*
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

import org.apache.commons.cli.ParseException;

/**
 *
 */
public class Main {
    public static void main(String[] args) {
        try {
            final GeneratorConfig config = new OptionParser().parse(args);
            config.generate();
        } catch (ParseException e) {
            new OptionParser().showHelp();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Problem generating RAML documentation.");
            e.printStackTrace();
            System.exit(1);
        }
    }

}
