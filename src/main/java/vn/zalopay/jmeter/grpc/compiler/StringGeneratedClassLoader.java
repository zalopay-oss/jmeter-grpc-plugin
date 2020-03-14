package vn.zalopay.jmeter.grpc.compiler;

import java.util.HashMap;
import java.util.Map;

public final class StringGeneratedClassLoader extends ClassLoader {

  private final Map<String, StringGeneratedClassFileObject> fileObjectMap = new HashMap<>();

  public StringGeneratedClassLoader(ClassLoader parent) {
    super(parent);
  }

  @Override
  protected Class<?> findClass(String fullClassName) throws ClassNotFoundException {
    StringGeneratedClassFileObject fileObject = fileObjectMap.get(fullClassName);
    if (fileObject != null) {
      byte[] classBytes = fileObject.getClassBytes();
      return defineClass(fullClassName, classBytes, 0, classBytes.length);
    }
    return super.findClass(fullClassName);
  }

  public void addJavaFileObject(String qualifiedName, StringGeneratedClassFileObject fileObject) {
    fileObjectMap.put(qualifiedName, fileObject);
  }
}
