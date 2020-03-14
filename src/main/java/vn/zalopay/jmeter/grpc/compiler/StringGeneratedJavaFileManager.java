package vn.zalopay.jmeter.grpc.compiler;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

public final class StringGeneratedJavaFileManager
    extends ForwardingJavaFileManager<JavaFileManager> {

  private final StringGeneratedClassLoader classLoader;

  public StringGeneratedJavaFileManager(
      JavaFileManager fileManager, StringGeneratedClassLoader classLoader) {
    super(fileManager);
    this.classLoader = classLoader;
  }

  @Override
  public JavaFileObject getJavaFileForOutput(
      Location location, String qualifiedName, Kind kind, FileObject sibling) {
    if (kind != Kind.CLASS) {
      throw new IllegalArgumentException(
          "Unsupported kind (" + kind + ") for class (" + qualifiedName + ").");
    }
    StringGeneratedClassFileObject fileObject = new StringGeneratedClassFileObject(qualifiedName);
    classLoader.addJavaFileObject(qualifiedName, fileObject);
    return fileObject;
  }

  @Override
  public ClassLoader getClassLoader(Location location) {
    return classLoader;
  }
}
