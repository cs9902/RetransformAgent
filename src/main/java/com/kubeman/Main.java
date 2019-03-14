package com.kubeman;

import com.sun.tools.attach.VirtualMachine;

public class Main {
    public static void main(String[] args) {

        String pid = args[0];

        try {
            VirtualMachine vm = VirtualMachine.attach(pid);
            vm.loadAgent(jarPath(), args[1]);

            if (args.length == 2 || (args.length == 3 && args[2].equals("detach"))) {
                vm.detach();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static String jarPath() {
        String path = RetransformAgent.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        try {
            path = java.net.URLDecoder.decode(path, "UTF-8");
        } catch (java.io.UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }

        java.io.File jarFile = new java.io.File(path);
        return jarFile.getAbsolutePath();
    }

}
