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

import guru.nidi.loader.Loader;
import guru.nidi.loader.basic.FileLoader;
import guru.nidi.loader.basic.UriLoader;
import guru.nidi.raml.doc.st.Feature;
import org.apache.commons.cli.*;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static org.apache.commons.cli.OptionBuilder.withDescription;

/**
 *
 */
public class OptionParser {
    public GeneratorConfig parse(String[] args) throws ParseException {
        final String[][] ramlsAndOptions = preparseArgs(args);
        if (ramlsAndOptions[0].length == 0) {
            throw new ParseException("Missing RAML file");
        }
        final CommandLine cmd = new BasicParser().parse(createOptions(), ramlsAndOptions[1]);
        return new GeneratorConfig(
                Arrays.asList(ramlsAndOptions[0]),
                parseTarget(cmd.getOptionValue('t')),
                parseFeatures(cmd.getOptionValue('f')),
                cmd.getOptionValue('b'),
                cmd.getOptionValue('p'),
                parseCustomization(cmd.getOptionValue('c'), ramlsAndOptions[0][0]),
                false);
    }

    private Loader parseCustomization(String c, String ramlLocation) {
        final String custom = (c != null ? c : GeneratorConfig.getBaseOfRaml(ramlLocation));
        return new UriLoader(new FileLoader(new File(custom)));
    }

    private File parseTarget(String t) {
        if (t == null) {
            return new File("raml-doc");
        }
        return new File(t);
    }

    private EnumSet<Feature> parseFeatures(String s) {
        return Feature.parse(s);
    }

    @SuppressWarnings("static-access")
    protected Options createOptions() {
        final String features = StringUtils.join(EnumSet.allOf(Feature.class).toArray(), ", ").toLowerCase();
        return new Options()
                .addOption(withDescription("Target directory to write the output.\nDefault: raml-doc").isRequired(false).withArgName("Directory").hasArg(true).create('t'))
                .addOption(withDescription("Features to enable.\nComma separated list of these features: " + features + "\nDefault: " + features).isRequired(false).withArgName("Features").hasArg(true).create('f'))
                .addOption(withDescription("The base URI to use.\nDefault: As defined in RAML").isRequired(false).withArgName("URI").hasArg(true).create('b'))
                .addOption(withDescription("Base URI parameters.\nFormat: parameter=value,...").isRequired(false).withArgName("Parameters").hasArg(true).create('p'))
                .addOption(withDescription("Customization location.\nfavicon.ico, custom-variables.less, custom-style.less are taken from there, if existing\nDefault: Location of RAML-1").isRequired(false).withArgName("Directory").hasArg(true).create('c'));
    }

    public void showHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(80);
        formatter.setOptionComparator(optionComparator());
        formatter.printHelp("java -jar raml-doc-standalone.jar RAML-1 [RAML-2 ...] [options]", helpHeader(), createOptions(), "", false);
    }

    protected String helpHeader() {
        return "\nRAML files can be referenced by different protocols:\n" +
                "- filename\n" +
                "- [user:pass@]http(s)://\n" +
                "- [token@]github://user/project/file\n" +
                "- user:pass@apiportal://\n" +
                "Options:\n";
    }

    protected OptionComparator optionComparator() {
        return new OptionComparator("tfbpc");
    }

    protected String[][] preparseArgs(String[] args) {
        final List<String> ramls = new ArrayList<>();
        final List<String> options = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.charAt(0) == '-') {
                if (arg.length() > 2) {
                    options.add(arg.substring(0, 2));
                    options.add(arg.substring(2));
                } else {
                    options.add(arg);
                    if (i < args.length - 1) {
                        options.add(args[++i]);
                    }
                }
            } else {
                ramls.add(arg);
            }
        }
        return new String[][]{toArray(ramls), toArray(options)};
    }

    protected String[] toArray(List<String> ss) {
        return ss.toArray(new String[ss.size()]);
    }

}
