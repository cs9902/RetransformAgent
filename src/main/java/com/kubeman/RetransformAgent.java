package com.kubeman;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kubeman.target.TargetTask;
import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * {
 *     "class-name": "com.kubeman.target.TargetTask",
 *     "method-name": "doTask",
 *     "before": "",
 *     "after": "",
 *     "final": "",
 *     "at": [
 *          {
 *          "line": 123,
 *          "code": ""
 *          }
 *     ]
 * }
 *
 */
public class RetransformAgent {

    public static void agentmain(String args, Instrumentation inst) {
        JSONObject jsonObject = JSON.parseObject(args);
        String targetClass = jsonObject.getString("class-name");
        String targetMethod = jsonObject.getString("method-name");
        String beforeCode = jsonObject.getString("before");
        String afterCode = jsonObject.getString("after");
        String finalCode = jsonObject.getString("final");

        Map<Integer, String> injectCodes = new HashMap<>();
        JSONArray jsonAtCodes = jsonObject.getJSONArray("at");
        for (int i = 0; i < jsonAtCodes.size(); i++) {
            JSONObject obj = jsonAtCodes.getJSONObject(i);
            int lineNum = obj.getInteger("line");
            String code = obj.getString("code");
            injectCodes.put(lineNum, code);
        }


        inst.addTransformer(new ClassFileTransformer() {
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer)
                    throws IllegalClassFormatException {

                String oriClassName = className.replaceAll("/", ".");

                if (oriClassName.equals(targetClass)) {
                    ClassPool classPool = ClassPool.getDefault();
                    try {
                        CtClass class1 = classPool.get(oriClassName);
                        CtMethod ctMethod = class1.getDeclaredMethod(targetMethod);
                        if (!ctMethod.isEmpty()) {

                            if (!isBlank(beforeCode)) {
                                ctMethod.insertBefore(beforeCode);
                            }

                            if (!isBlank(afterCode)) {
                                ctMethod.insertAfter(afterCode, false);
                            }

                            if (!isBlank(finalCode)) {
                                ctMethod.insertAfter(finalCode, true);
                            }

                            for (Integer line : injectCodes.keySet()) {
                                String lineCode = injectCodes.get(line);
                                if (!isBlank(lineCode)) {
                                    ctMethod.insertAt(line, lineCode);
                                }
                            }

                            System.out.println("success");
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

            if (clazz.getName().equals(TargetTask.class.getName())) {
                System.out.println("retransform");
                try {
                    inst.retransformClasses(clazz);
                } catch (Throwable e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private static boolean isBlank(String string) {
        return string == null || string.trim().equals("");
    }

}
