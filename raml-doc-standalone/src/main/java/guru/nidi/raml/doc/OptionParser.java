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

import guru.nidi.raml.doc.st.Feature;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.apache.commons.cli.OptionBuilder.withDescription;

/**
 *
 */
public class OptionParser {
    public GeneratorConfig parse(String[] args) throws ParseException {
        final CommandLine cmd = new BasicParser().parse(createOptions(), expandArgs(args));
        return new GeneratorConfig(
                cmd.getOptionValue('r'),
                parseTarget(cmd.getOptionValue('t')),
                parseFeatures(cmd.getOptionValue('f')),
                cmd.getOptionValue('m'),
                cmd.getOptionValue('b'),
                cmd.getOptionValue('p'));
    }


    private File parseTarget(String t) {
        if (t == null) {
            return new File(".");
        }
        return new File(t);
    }

    private EnumSet<Feature> parseFeatures(String s) {
        return Feature.parse(s);
    }

    @SuppressWarnings("static-access")
    protected Options createOptions() {
        return new Options()
                .addOption(withDescription("The RAML resource\n" +
                        "Format: classpath://, file://,\n" +
                        "[user:pass@]http://, [user:pass@]https://,\n" +
                        "[token@]github://user/project/file, user:pass@apiportal://").isRequired(true).withArgName("URL").hasArg(true).create('r'))
                .addOption(withDescription("Target directory to write the output\nDefault: current directory").isRequired(false).withArgName("Directory").hasArg(true).create('t'))
                .addOption(withDescription("Enable features\nComma separated list of these features: download,tryout\nDefault: download,tryout").isRequired(false).hasArg(true).create('f'))
                .addOption(withDescription("The parent title if there is more than one RAML\nDefault: The title for the first RAML").isRequired(false).withArgName("Title").hasArg(true).create('m'))
                .addOption(withDescription("The base URI to use\nDefault: As defined in RAML").isRequired(false).withArgName("URI").hasArg(true).create('b'))
                .addOption(withDescription("Base URI parameters\nFormat: parameter=value,...").isRequired(false).withArgName("Parameters").hasArg(true).create('p'));
    }

    public void showHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(80);
        formatter.setOptionComparator(optionComparator());
        formatter.printHelp("java -jar raml-doc-standalone.jar", helpHeader(), createOptions(), "", true);
    }

    protected String helpHeader() {
        return "";
    }

    protected OptionComparator optionComparator() {
        return new OptionComparator("rtimbp");
    }

    protected String[] expandArgs(String[] args) {
        final List<String> res = new ArrayList<>();
        for (String arg : args) {
            if (arg.charAt(0) == '-' && arg.length() > 2) {
                res.add(arg.substring(0, 2));
                res.add(arg.substring(2));
            } else {
                res.add(arg);
            }
        }
        return res.toArray(new String[res.size()]);
    }

}
