package com.ccs.javadroid.tools.bytecode;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DependencyModel {

    public static final class ClassNode {
        public final String name;
        public final String simpleName;
        public final String packageName;
        public String superClass;
        public final List<String> interfaces = new ArrayList<>();
        public final List<String> usedBy = new ArrayList<>();
        public final List<String> dependsOn = new ArrayList<>();
        public int methodCount;
        public int fieldCount;
        public int linesOfCode;

        public ClassNode(String name) {
            this.name = name;
            int lastDot = name.lastIndexOf('.');
            this.simpleName = lastDot >= 0 ? name.substring(lastDot + 1) : name;
            this.packageName = lastDot >= 0 ? name.substring(0, lastDot) : "";
        }
    }

    public static final class DependencyEdge {
        public final String from;
        public final String to;
        public final DependencyType type;

        public DependencyEdge(String from, String to, DependencyType type) {
            this.from = from;
            this.to = to;
            this.type = type;
        }
    }

    public enum DependencyType {
        EXTENDS("extends"),
        IMPLEMENTS("implements"),
        USES("uses"),
        FIELD("field"),
        PARAMETER("parameter"),
        RETURNS("returns");

        public final String label;
        DependencyType(String label) { this.label = label; }
    }

    private final Map<String, ClassNode> classes = new HashMap<>();
    private final List<DependencyEdge> edges = new ArrayList<>();

    public Map<String, ClassNode> getClasses() { return classes; }
    public List<DependencyEdge> getEdges() { return edges; }

    public int analyzeDirectory(File dir) throws IOException {
        int count = 0;
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        count += analyzeDirectory(f);
                    } else if (f.getName().endsWith(".class")) {
                        try {
                            analyzeClass(f);
                            count++;
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
        return count;
    }

    public void analyzeClass(File classFile) throws IOException {
        try (InputStream is = new FileInputStream(classFile)) {
            analyzeClass(is);
        }
    }

    public void analyzeClass(InputStream classStream) throws IOException {
        ClassReader cr = new ClassReader(classStream);
        final String[] currentClass = {null};
        final String[] currentSuper = {null};
        final List<String> currentInterfaces = new ArrayList<>();
        final Set<String> usedClasses = new HashSet<>();

        cr.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public void visit(int version, int access, String name, String signature,
                              String superName, String[] interfaces) {
                currentClass[0] = name.replace('/', '.');
                currentSuper[0] = superName != null ? superName.replace('/', '.') : null;
                if (interfaces != null) {
                    for (String iface : interfaces) {
                        currentInterfaces.add(iface.replace('/', '.'));
                    }
                }
            }

            @Override
            public FieldVisitor visitField(int access, String name, String desc,
                                           String signature, Object value) {
                addTypeRefs(desc, usedClasses);
                if (signature != null) addTypeRefs(signature, usedClasses);
                return null;
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc,
                                             String signature, String[] exceptions) {
                addTypeRefs(desc, usedClasses);
                if (signature != null) addTypeRefs(signature, usedClasses);
                if (exceptions != null) {
                    for (String ex : exceptions) usedClasses.add(ex.replace('/', '.'));
                }

                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name,
                                                String desc, boolean isInterface) {
                        usedClasses.add(owner.replace('/', '.'));
                        addTypeRefs(desc, usedClasses);
                    }

                    @Override
                    public void visitFieldInsn(int opcode, String owner, String name,
                                               String desc) {
                        usedClasses.add(owner.replace('/', '.'));
                    }

                    @Override
                    public void visitTypeInsn(int opcode, String type) {
                        usedClasses.add(type.replace('/', '.'));
                    }

                    @Override
                    public void visitLdcInsn(Object cst) {
                        if (cst instanceof Type) {
                            Type t = (Type) cst;
                            usedClasses.add(t.getClassName());
                        }
                    }
                };
            }
        }, 0);

        if (currentClass[0] == null) return;

        ClassNode cn = getOrCreateClass(currentClass[0]);

        if (currentSuper[0] != null && !"java.lang.Object".equals(currentSuper[0])) {
            cn.superClass = currentSuper[0];
            addEdge(currentClass[0], currentSuper[0], DependencyType.EXTENDS);
        }

        for (String iface : currentInterfaces) {
            cn.interfaces.add(iface);
            addEdge(currentClass[0], iface, DependencyType.IMPLEMENTS);
        }

        for (String used : usedClasses) {
            if (used.equals(currentClass[0])) continue;
            if (used.startsWith("java.") || used.startsWith("javax.") || used.startsWith("android.") || used.startsWith("androidx.")) continue;

            if (!cn.dependsOn.contains(used)) cn.dependsOn.add(used);

            ClassNode usedNode = getOrCreateClass(used);
            if (!usedNode.usedBy.contains(currentClass[0])) {
                usedNode.usedBy.add(currentClass[0]);
            }

            boolean isField = false;
            for (DependencyEdge e : edges) {
                if (e.from.equals(currentClass[0]) && e.to.equals(used)
                        && (e.type == DependencyType.EXTENDS || e.type == DependencyType.IMPLEMENTS)) {
                    isField = true;
                    break;
                }
            }
            if (!isField) {
                addEdge(currentClass[0], used, DependencyType.USES);
            }
        }
    }

    private void addTypeRefs(String descriptor, Set<String> usedClasses) {
        Type[] types;
        try {
            if (descriptor.contains(")")) {
                Type methodType = Type.getMethodType(descriptor);
                usedClasses.add(methodType.getReturnType().getClassName());
                for (Type t : methodType.getArgumentTypes()) {
                    usedClasses.add(t.getClassName());
                }
            } else {
                Type t = Type.getType(descriptor);
                usedClasses.add(t.getClassName());
            }
        } catch (Exception ignored) {
        }

        if (descriptor.contains("<")) {
            int start = descriptor.indexOf('<');
            int end = descriptor.lastIndexOf('>');
            if (start >= 0 && end > start) {
                String generic = descriptor.substring(start + 1, end);
                for (String part : generic.split(";")) {
                    String clean = part.replace('/', '.').replace("L", "").replace("T", "");
                    if (clean.contains(".")) usedClasses.add(clean);
                }
            }
        }
    }

    private void addEdge(String from, String to, DependencyType type) {
        for (DependencyEdge e : edges) {
            if (e.from.equals(from) && e.to.equals(to) && e.type == type) return;
        }
        edges.add(new DependencyEdge(from, to, type));
    }

    private ClassNode getOrCreateClass(String name) {
        return classes.computeIfAbsent(name, ClassNode::new);
    }

    public List<ClassNode> getProjectClasses() {
        List<ClassNode> result = new ArrayList<>();
        for (ClassNode cn : classes.values()) {
            if (!cn.name.startsWith("java.") && !cn.name.startsWith("javax.")
                    && !cn.name.startsWith("android.") && !cn.name.startsWith("androidx.")) {
                result.add(cn);
            }
        }
        return result;
    }

    public Set<String> getProjectPackages() {
        Set<String> packages = new HashSet<>();
        for (ClassNode cn : getProjectClasses()) {
            if (!cn.packageName.isEmpty()) {
                packages.add(cn.packageName);
            }
        }
        return packages;
    }

    public int getInDegree(String className) {
        ClassNode cn = classes.get(className);
        return cn != null ? cn.usedBy.size() : 0;
    }

    public int getOutDegree(String className) {
        ClassNode cn = classes.get(className);
        return cn != null ? cn.dependsOn.size() : 0;
    }

    public List<String> findCircularDependencies() {
        Set<String> visited = new HashSet<>();
        Set<String> inStack = new HashSet<>();
        List<String> cycles = new ArrayList<>();

        for (ClassNode cn : getProjectClasses()) {
            if (!visited.contains(cn.name)) {
                detectCycle(cn.name, visited, inStack, cycles, new ArrayList<>());
            }
        }
        return cycles;
    }

    private void detectCycle(String current, Set<String> visited, Set<String> inStack,
                             List<String> cycles, List<String> path) {
        visited.add(current);
        inStack.add(current);
        path.add(current);

        ClassNode cn = classes.get(current);
        if (cn != null) {
            for (String dep : cn.dependsOn) {
                if (!visited.contains(dep)) {
                    detectCycle(dep, visited, inStack, cycles, path);
                } else if (inStack.contains(dep)) {
                    StringBuilder cycle = new StringBuilder();
                    int idx = path.indexOf(dep);
                    for (int i = idx; i < path.size(); i++) {
                        if (i > idx) cycle.append(" → ");
                        cycle.append(path.get(i));
                    }
                    cycle.append(" → ").append(dep);
                    cycles.add(cycle.toString());
                }
            }
        }

        inStack.remove(current);
        path.remove(path.size() - 1);
    }
}
