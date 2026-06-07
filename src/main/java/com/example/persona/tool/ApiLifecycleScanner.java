package com.example.persona.tool;

import com.example.persona.config.annotation.ApiLifecycle;
import com.example.persona.utils.VersionUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Scanner tool to find deprecated APIs before a given version. Can be run as a
 * standalone tool or via Gradle task.
 */
public class ApiLifecycleScanner {

    private final String targetVersion;
    private final String outputFile;
    private final List<ApiInfo> deprecatedApis = new ArrayList<>();

    public ApiLifecycleScanner(String targetVersion, String outputFile) {
        this.targetVersion = targetVersion;
        this.outputFile = outputFile;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: ApiLifecycleScanner <targetVersion> [outputFile]");
            System.err.println("Example: ApiLifecycleScanner 2.0.0 deprecated-apis.txt");
            System.exit(1);
        }

        String targetVersion = args[0];
        String outputFile = args.length > 1 ? args[1] : null;

        ApiLifecycleScanner scanner = new ApiLifecycleScanner(targetVersion, outputFile);
        scanner.scan();
        scanner.printResults();
    }

    public void scan() {
        System.out.println("Scanning for APIs deprecated before version: " + targetVersion);
        System.out.println("=".repeat(80));

        try {
            // Get classpath
            String classpath = System.getProperty("java.class.path");
            String[] paths = classpath.split(File.pathSeparator);

            for (String path : paths) {
                File file = new File(path);
                if (file.isDirectory()) {
                    scanDirectory(file, "");
                } else if (file.getName().endsWith(".jar")) {
                    scanJar(file);
                }
            }
        } catch (Exception e) {
            System.err.println("Error during scan: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void scanDirectory(File directory, String packageName) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                String newPackage = packageName.isEmpty() ? file.getName() : packageName + "." + file.getName();
                scanDirectory(file, newPackage);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName.isEmpty()
                        ? file.getName().replace(".class", "")
                        : packageName + "." + file.getName().replace(".class", "");
                processClass(className);
            }
        }
    }

    private void scanJar(File jarFile) {
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class") && entry.getName().contains("com/example/persona/controller")) {
                    String className = entry.getName().replace("/", ".").replace(".class", "");
                    processClass(className);
                }
            }
        } catch (IOException e) {
            // Skip jar files that can't be read
        }
    }

    private void processClass(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (!isController(clazz)) {
                return;
            }

            // Check class-level annotation
            ApiLifecycle classAnnotation = clazz.getAnnotation(ApiLifecycle.class);
            if (classAnnotation != null && isDeprecatedBefore(classAnnotation)) {
                deprecatedApis.add(createApiInfo(clazz, null, null, classAnnotation));
            }

            // Check method-level annotations
            for (Method method : clazz.getDeclaredMethods()) {
                ApiLifecycle methodAnnotation = method.getAnnotation(ApiLifecycle.class);
                if (methodAnnotation != null && isDeprecatedBefore(methodAnnotation)) {
                    String mapping = getMappingPath(method);
                    deprecatedApis.add(createApiInfo(clazz, method, mapping, methodAnnotation));
                }
            }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            // Skip classes that can't be loaded
        }
    }

    private boolean isController(Class<?> clazz) {
        return clazz.isAnnotationPresent(RestController.class) || clazz.isAnnotationPresent(Controller.class);
    }

    private boolean isDeprecatedBefore(ApiLifecycle annotation) {
        String deprecatedVersion = annotation.deprecated();
        if (deprecatedVersion == null || deprecatedVersion.trim().isEmpty()) {
            return false;
        }
        return VersionUtils.isDeprecatedBefore(deprecatedVersion, targetVersion);
    }

    private ApiInfo createApiInfo(Class<?> clazz, Method method, String mapping, ApiLifecycle annotation) {
        ApiInfo info = new ApiInfo();
        info.type = method == null ? "CLASS" : "METHOD";
        info.className = clazz.getSimpleName();
        info.fullClassName = clazz.getName();
        if (method != null) {
            info.methodName = method.getName();
            info.mapping = mapping;
        }
        info.since = annotation.since();
        info.deprecated = annotation.deprecated();
        info.removed = annotation.removed();
        info.message = annotation.message();
        info.alternative = annotation.alternative();
        return info;
    }

    private String getMappingPath(Method method) {
        List<String> mappings = new ArrayList<>();

        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        if (getMapping != null) {
            String[] paths = getMapping.value().length > 0 ? getMapping.value() : getMapping.path();
            if (paths.length > 0) {
                mappings.add("GET " + paths[0]);
            }
        }

        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        if (postMapping != null) {
            String[] paths = postMapping.value().length > 0 ? postMapping.value() : postMapping.path();
            if (paths.length > 0) {
                mappings.add("POST " + paths[0]);
            }
        }

        PutMapping putMapping = method.getAnnotation(PutMapping.class);
        if (putMapping != null) {
            String[] paths = putMapping.value().length > 0 ? putMapping.value() : putMapping.path();
            if (paths.length > 0) {
                mappings.add("PUT " + paths[0]);
            }
        }

        DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
        if (deleteMapping != null) {
            String[] paths = deleteMapping.value().length > 0 ? deleteMapping.value() : deleteMapping.path();
            if (paths.length > 0) {
                mappings.add("DELETE " + paths[0]);
            }
        }

        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        if (requestMapping != null) {
            String[] paths = requestMapping.value().length > 0 ? requestMapping.value() : requestMapping.path();
            String methodName = requestMapping.method().length > 0 ? requestMapping.method()[0].name() : "GET";
            if (paths.length > 0) {
                mappings.add(methodName + " " + paths[0]);
            }
        }

        return mappings.isEmpty() ? "N/A" : String.join(", ", mappings);
    }

    public void printResults() {
        if (deprecatedApis.isEmpty()) {
            System.out.println("\n✓ No APIs deprecated before version " + targetVersion);
        } else {
            System.out.println("\nFound " + deprecatedApis.size() + " deprecated API(s):\n");
            for (ApiInfo api : deprecatedApis) {
                System.out.println(formatApiInfo(api));
            }

            if (outputFile != null) {
                try (FileWriter writer = new FileWriter(outputFile)) {
                    for (ApiInfo api : deprecatedApis) {
                        writer.write(formatApiInfo(api) + "\n\n");
                    }
                    System.out.println("\n✓ Results written to: " + outputFile);
                } catch (IOException e) {
                    System.err.println("Error writing to file: " + e.getMessage());
                }
            }
        }
    }

    private String formatApiInfo(ApiInfo api) {
        StringBuilder sb = new StringBuilder();
        sb.append(api.type).append(": ").append(api.className);
        if (api.methodName != null) {
            sb.append(".").append(api.methodName).append("()");
        }
        sb.append("\n");
        sb.append("  Class: ").append(api.fullClassName).append("\n");
        if (api.mapping != null && !api.mapping.equals("N/A")) {
            sb.append("  Endpoint: ").append(api.mapping).append("\n");
        }
        sb.append("  Since: ").append(api.since).append("\n");
        sb.append("  Deprecated: ").append(api.deprecated).append("\n");
        if (api.removed != null && !api.removed.isEmpty()) {
            sb.append("  Removed: ").append(api.removed).append("\n");
        }
        if (api.message != null && !api.message.isEmpty()) {
            sb.append("  Message: ").append(api.message).append("\n");
        }
        if (api.alternative != null && !api.alternative.isEmpty()) {
            sb.append("  Alternative: ").append(api.alternative).append("\n");
        }
        return sb.toString();
    }

    private static class ApiInfo {
        String type;
        String className;
        String fullClassName;
        String methodName;
        String mapping;
        String since;
        String deprecated;
        String removed;
        String message;
        String alternative;
    }
}
