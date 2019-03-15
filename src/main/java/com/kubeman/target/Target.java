package com.kubeman.target;

public class Target {
    public static void main(String[] args) {

        TargetTask task = new TargetTask();

        while (true) {

            task.doTask();

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }

        }

    }
}
