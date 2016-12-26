package com.example;

import com.example.Traffic.Car;
import com.example.Traffic.Road;
import com.example.Traffic.TrafficManager;
import com.sun.istack.internal.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {

        TrafficManager manager = new TrafficManager();
        manager.addRule(new Car.Rule() {
            @Override
            public void onMove(
                    Car car,
                    @Nullable Road left,
                    @Nullable Road right,
                    int forwardFreeLength,
                    int backFreeLength,
                    int nextJamLength,
                    boolean wasSecondOfJam) {
                //Define your rules here.
                if (nextJamLength < 2 &&
                        backFreeLength > 1) {
                    //Don't move.
                } else {
                    car.move(1);
                }
                super.onMove(car, left, right, forwardFreeLength, backFreeLength, nextJamLength, wasSecondOfJam);
            }
        });

        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);
        String roadCondition = null;

        do {
            manager.clear();

            //道路パターンを入力
            System.out.println("道路入力");
            System.out.println("例1：□■■□□■□");
            System.out.println("例2：0110010");
            System.out.println("車あり：■または1");
            System.out.println("車なし：□または0");
            try {
                roadCondition = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Road road = manager.addRoad(roadCondition);

            //経過時間を入力
            String timesStr;
            int times;
            System.out.println("経過時間を入力");
            System.out.println("例：10");
            try {
                timesStr = br.readLine();
                times = Integer.parseInt(timesStr);
            } catch (IOException | NumberFormatException e) {
                times = 10;
            }

            //実行結果の出力
            System.out.println(road.toString());
            for (int i = 0; i < times; i++) {
                manager.update();
                System.out.println(road.toString());
            }

            //再実行するか確認する
            String yn;
            System.out.println("再実行？ (y/n)");
            try {
                yn = br.readLine();
                if (yn.charAt(0) != 'y') {
                    break;
                }
            } catch (IOException | StringIndexOutOfBoundsException e) {
                break;
            }
        } while (true);

        try {
            isr.close();
            br.close();
        } catch (IOException e) {
            //ignore
            e.printStackTrace();
        }
    }
}
