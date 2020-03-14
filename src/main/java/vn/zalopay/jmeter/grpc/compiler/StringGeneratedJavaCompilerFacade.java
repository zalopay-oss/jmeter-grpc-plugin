package vn.zalopay.jmeter.grpc.compiler;

import javax.tools.JavaCompiler;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class StringGeneratedJavaCompilerFacade {

  private final StringGeneratedClassLoader classLoader;
  private final JavaCompiler compiler;
  private final DiagnosticCollector<JavaFileObject> diagnosticCollector;

  public StringGeneratedJavaCompilerFacade(ClassLoader loader) {
    compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      throw new IllegalStateException(
          "Cannot find the system Java compiler.\n"
              + "Maybe you're using the JRE without the JDK: either the classpath lacks a jar (tools.jar)"
              + " xor the modulepath lacks a module (java.compiler).");
    }
    classLoader = new StringGeneratedClassLoader(loader);
    diagnosticCollector = new DiagnosticCollector<>();
  }

  protected String getCompilationMessages(Pattern linePattern, String javaSource) {
    return diagnosticCollector.getDiagnostics().stream()
        .map(
            d -> {
              String resp =
                  String.format(
                      "%s:[%d,%d] %s%n",
                      d.getKind().toString(),
                      d.getLineNumber(),
                      d.getColumnNumber(),
                      d.getMessage(null));

              return d.getLineNumber() <= 0
                  ? resp
                  : resp.concat(
                      linePattern
                          .splitAsStream(javaSource)
                          .skip(d.getLineNumber() - 1)
                          .findFirst()
                          .orElse(""));
            })
        .collect(Collectors.joining("\n"));
  }

  public synchronized <T> Class<? extends T> compile(
      String fullClassName, String javaSource, Class<T> superType) {
    StringGeneratedSourceFileObject fileObject;
    fileObject = new StringGeneratedSourceFileObject(fullClassName, javaSource);

    JavaFileManager standardFileManager =
        compiler.getStandardFileManager(diagnosticCollector, null, null);
    try (StringGeneratedJavaFileManager javaFileManager =
        new StringGeneratedJavaFileManager(standardFileManager, classLoader)) {
      CompilationTask task =
          compiler.getTask(
              null,
              javaFileManager,
              diagnosticCollector,
              null,
              null,
              Collections.singletonList(fileObject));
      boolean success = task.call();
      if (!success) {
        final Pattern linePattern = Pattern.compile("\n");
        String compilationMessages = getCompilationMessages(linePattern, javaSource);

        throw new IllegalStateException(
            String.format(
                "The generated class (%s) failed to compile.%n%s",
                fullClassName, compilationMessages));
      }
    } catch (IOException e) {
      throw new IllegalStateException(
          String.format(
              "The generated class (%s) failed to compile because the %s didn't close.",
              fullClassName, JavaFileManager.class.getSimpleName()),
          e);
    }

    Class<T> compiledClass;

    try {
      compiledClass = (Class<T>) classLoader.loadClass(fullClassName);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException(
          String.format("The generated class (%s) compiled, but failed to load.", fullClassName),
          e);
    }

    if (!superType.isAssignableFrom(compiledClass)) {
      throw new ClassCastException(
          String.format(
              "The generated compiledClass (%s) cannot be assigned to the superclass/interface (%s).",
              compiledClass, superType));
    }
    return compiledClass;
  }
}
