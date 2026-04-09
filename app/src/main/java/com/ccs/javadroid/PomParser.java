package com.ccs.javadroid;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Мінімальний розбір pom.xml: координати, properties, dependencies.
 */
public final class PomParser {

    private PomParser() {}

    public static PomModel parse(File pomFile) throws IOException, XmlPullParserException {
        try (FileReader reader = new FileReader(pomFile)) {
            return parse(reader);
        }
    }

    public static PomModel parse(Reader reader) throws XmlPullParserException, IOException {
        XmlPullParserFactory f = XmlPullParserFactory.newInstance();
        f.setNamespaceAware(true);
        XmlPullParser p = f.newPullParser();
        p.setInput(reader);

        PomModel model = new PomModel();
        boolean inProperties = false;
        boolean inDependency = false;
        boolean inParent = false;
        int propDepth = -1;
        PomModel.MavenDependency cur = null;
        String textTarget = null;

        int event = p.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                String name = localName(p);
                if ("parent".equals(name)) inParent = true;
                if ("properties".equals(name)) {
                    inProperties = true;
                    propDepth = p.getDepth();
                } else if ("dependency".equals(name)) {
                    inDependency = true;
                    cur = new PomModel.MavenDependency();
                } else if (inDependency && cur != null) {
                    if ("groupId".equals(name)) textTarget = "dg";
                    else if ("artifactId".equals(name)) textTarget = "da";
                    else if ("version".equals(name)) textTarget = "dv";
                    else if ("scope".equals(name)) textTarget = "ds";
                    else textTarget = null;
                } else if (!inDependency) {
                    if (inProperties && p.getDepth() == propDepth + 1) {
                        textTarget = "prop:" + name;
                    } else if (!inProperties && !inParent) {
                        int depth = p.getDepth();
                        if ("groupId".equals(name) && depth == 2) textTarget = "g";
                        else if ("artifactId".equals(name) && depth == 2) textTarget = "a";
                        else if ("version".equals(name) && depth == 2) textTarget = "v";
                        else if ("packaging".equals(name) && depth == 2) textTarget = "pkg";
                        else textTarget = null;
                    } else {
                        textTarget = null;
                    }
                }
            } else if (event == XmlPullParser.TEXT) {
                String text = p.getText();
                if (text == null || textTarget == null) {
                    event = p.next();
                    continue;
                }
                text = text.trim();
                if (text.isEmpty()) {
                    event = p.next();
                    continue;
                }
                if ("g".equals(textTarget)) model.groupId = text;
                else if ("a".equals(textTarget)) model.artifactId = text;
                else if ("v".equals(textTarget)) model.version = text;
                else if ("pkg".equals(textTarget)) model.packaging = text;
                else if (textTarget.startsWith("prop:")) {
                    model.properties.put(textTarget.substring(5), text);
                } else if (cur != null) {
                    if ("dg".equals(textTarget)) cur.groupId = text;
                    else if ("da".equals(textTarget)) cur.artifactId = text;
                    else if ("dv".equals(textTarget)) cur.version = text;
                    else if ("ds".equals(textTarget)) cur.scope = text;
                }
            } else if (event == XmlPullParser.END_TAG) {
                String name = localName(p);
                if ("parent".equals(name)) inParent = false;
                if ("properties".equals(name)) {
                    inProperties = false;
                    propDepth = -1;
                } else if ("dependency".equals(name) && cur != null) {
                    if (cur.groupId != null && cur.artifactId != null) {
                        model.dependencies.add(cur);
                    }
                    inDependency = false;
                    cur = null;
                }
                textTarget = null;
            }
            event = p.next();
        }

        String mc = model.properties.get("mainClass");
        if (mc != null) model.mainClass = model.resolveProperty(mc);

        for (PomModel.MavenDependency d : model.dependencies) {
            d.groupId = model.resolveProperty(d.groupId);
            d.artifactId = model.resolveProperty(d.artifactId);
            d.version = model.resolveProperty(d.version);
        }
        model.version = model.resolveProperty(model.version);
        model.groupId = model.resolveProperty(model.groupId);

        return model;
    }

    private static String localName(XmlPullParser p) {
        String n = p.getName();
        if (n == null) return "";
        int c = n.lastIndexOf(':');
        return c >= 0 ? n.substring(c + 1) : n;
    }
}
