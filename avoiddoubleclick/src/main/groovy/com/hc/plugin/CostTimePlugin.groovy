package com.hc.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

public class CostTimePlugin extends Transform implements Plugin<Project> {
  @Override public void apply(Project project) {
    def android = project.extensions.getByType(AppExtension)
    android.registerTransform(new CostTimePlugin())
  }

  @Override
  String getName() {
    return "CostTimePlugin"
  }

  @Override
  Set<QualifiedContent.ContentType> getInputTypes() {
    return TransformManager.CONTENT_CLASS
  }

  @Override
  Set<? super QualifiedContent.Scope> getScopes() {
    return TransformManager.SCOPE_FULL_PROJECT
  }

  @Override
  boolean isIncremental() {
    return false
  }

  @Override
  void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
    super.transform(transformInvocation)
    if (!incremental) {
      transformInvocation.outputProvider.deleteAll()
    }

    transformInvocation.inputs.each { TransformInput input ->
      input.directoryInputs.each { DirectoryInput directoryInput ->
        File dest = transformInvocation.outputProvider.getContentLocation(directoryInput.name,
                directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
        File dir = directoryInput.file

        if (dir) {
          HashMap<String, File> modifyMap = new HashMap<>()
          dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) {
            File classFile ->
              if (isShouldModify(classFile.name)) {
                File modified = modifyClassFile(dir, classFile, transformInvocation.context.getTemporaryDir())
                if (modified != null) {
                  String key = classFile.absolutePath.replace(dir.absolutePath, "")
                  modifyMap.put(key, modified)
                }
              }
          }
          FileUtils.copyDirectory(directoryInput.file, dest)
          modifyMap.entrySet().each {
            Map.Entry<String, File> en ->
              File target = new File(dest.absolutePath + en.getKey())
              if (target.exists()) {
                target.delete()
              }
              FileUtils.copyFile(en.getValue(), target)
              en.getValue().delete()
          }
        }
      }

      input.jarInputs.each { JarInput jarInput ->
        String destName = jarInput.file.name
        def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath).substring(0, 8)
        if (destName.endsWith(".jar")) {
          destName = destName.substring(0, destName.length() - 4)
        }

        File dest = transformInvocation.outputProvider.getContentLocation(destName + "_" + hexName,
                jarInput.contentTypes, jarInput.scopes, Format.JAR)
        FileUtils.copyFile(jarInput.file, dest)
      }
    }
  }

  static boolean isShouldModify(String className) {
    if (className.contains('R$') ||
            className.contains('R2$') ||
            className.contains('R.class') ||
            className.contains('R2.class') ||
            className.contains('BuildConfig.class')) {
      return false
    }
    return true
  }

  static String path2ClassName(String pathName) {
    pathName.replace(File.separator, ".").replace(".class", "")
  }

  static File modifyClassFile(File dir, File classFile, File tempDir) {
    File modified = null
    try {
      String className = path2ClassName(classFile.absolutePath.replace(dir.absolutePath + File.separator, ""))
      byte[] sourceClassBytes = IOUtils.toByteArray(new FileInputStream(classFile))
      byte[] modifiedClassBytes = modifyClass(sourceClassBytes)
      if (modifiedClassBytes) {
        modified = new File(tempDir, className.replace('.', '') + '.class')
        if (modified.exists()) {
          modified.delete()
        }
        modified.createNewFile()
        new FileOutputStream(modified).write(modifiedClassBytes)
      }
    } catch (Exception e) {
      e.printStackTrace()
      modified = classFile
    }
    return modified
  }

  private static byte[] modifyClass(byte[] srcClass) throws IOException {
    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
    ClassVisitor classVisitor = new CostClassVisitor(classWriter)
    ClassReader cr = new ClassReader(srcClass)
    cr.accept(classVisitor, ClassReader.SKIP_FRAMES)
    return classWriter.toByteArray()
  }
}
