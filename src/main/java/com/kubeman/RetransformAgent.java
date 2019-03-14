package com.kubeman;


import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

public class RetransformAgent {

    public static void agentmain(String args, Instrumentation inst) {


        inst.addTransformer(new ClassFileTransformer() {
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer)
                    throws IllegalClassFormatException {
                String oriClassName = className.replaceAll("/", ".");

                if (className.equals("Main")) {
                    ClassPool classPool = ClassPool.getDefault();
                    try {
                        CtClass class1 = classPool.get(oriClassName);
                        CtMethod ctMethod = class1.getDeclaredMethod("sayHello");
                        if (!ctMethod.isEmpty()) {
                            ctMethod.insertBefore("System.out.println(\"before hello!!!\");");
                        }
                        return class1.toBytecode();
                    } catch (NotFoundException | CannotCompileException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return new byte[0];
            }
        }, true);

        Class[] classes = inst.getAllLoadedClasses();
        for (Class clazz : classes) {
            if (clazz.getName().equals("")) {

                try {
                    inst.retransformClasses(clazz);
                } catch (UnmodifiableClassException e) {
                    e.printStackTrace();
                }

            }
        }
    }

}
