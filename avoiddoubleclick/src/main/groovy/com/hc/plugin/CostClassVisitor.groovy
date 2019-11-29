package com.hc.plugin;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import javax.naming.Name;

/**
 * 巴掌
 * https://github.com/JeasonWong
 */

public class CostClassVisitor extends ClassVisitor {
  CostClassVisitor(ClassVisitor cv) {
    super(Opcodes.ASM5, cv)
  }

  @Override
  MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions)
    return new CostMethodVisitor(methodVisitor, access, name, desc)
  }

  static class CostMethodVisitor extends AdviceAdapter {
    private final String methodName
    private final int access
    private final String desc
    private final MethodVisitor methodVisitor
    private boolean inject = false;

    @SuppressWarnings("UnnecessaryQualifiedReference")
    protected CostMethodVisitor(MethodVisitor mv, int access, String name, String desc) {
      super(Opcodes.ASM5, mv, access, name, desc)
      this.methodVisitor = mv
      this.methodName = name
      this.access = access
      this.desc = desc

      if (name == "onClick" && desc == "(Landroid/view/View;)V") {
        inject = true
      }
    }

    @Override
    AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      //含有此注解的,跳过判断
      if (desc == "Lcom/example/myapplication/DoubleCLick;") {
        inject = false
      }
      return super.visitAnnotation(desc, visible)
    }


    @Override
    protected void onMethodEnter() {
      if (inject) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I", false);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESTATIC, "com/example/myapplication/XClickUtil", "isFastDoubleClick", "(ILandroid/view/View;)Z", false);
        Label l1 = new Label();
        mv.visitJumpInsn(IFEQ, l1);
        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitLineNumber(51, l2);
        mv.visitInsn(RETURN);
        mv.visitLabel(l1);
        mv.visitLineNumber(53, l1);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
      }
    }
  }
}
