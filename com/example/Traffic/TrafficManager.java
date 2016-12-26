package com.example.Traffic;

import com.sun.istack.internal.Nullable;

import java.util.ArrayList;

/**
 * 車と道路を管理するクラス.
 * Created by fr007 on 2016/05/27.
 */
public class TrafficManager {
    private ArrayList<Road> roads;
    private Car.Rule rule;

    /**
     * TrafficManagerを生成する.
     */
    public TrafficManager() {
        roads = new ArrayList<>();
    }

    /**
     * 管理している道のリストを返す.
     *
     * @return 管理している道のリスト
     */
    public ArrayList<Road> getRoads() {
        return roads;
    }

    /**
     * 文字列から道を追加する.
     * <p>
     * 車：■または1
     * 空き：□または0
     * <p>
     * 初めて追加する場合はそのまま追加.
     * 2本目以降の場合は1本目と同じ長さにあわせる.
     *
     * @param roadCondition 文字列
     * @return 追加した道
     */
    public Road addRoad(String roadCondition) {
        Road created = null;

        if (roads.size() == 0) {
            created = new Road(roadCondition, this);
            roads.add(created);
        } else {
            created = new Road(roads.get(0).length, this);
            StringBuilder cleanedRoadCondition = new StringBuilder();
            for (int i = 0; i < roadCondition.length(); i++) {
                char roadChar = roadCondition.charAt(i);
                if (roadChar == '■'
                        || roadChar == '□'
                        || roadChar == '1'
                        || roadChar == '0') {
                    cleanedRoadCondition.append(roadChar);
                }
            }

            for (int i = 0; i < cleanedRoadCondition.length(); i++) {
                if (i == created.length)
                    break;

                char roadChar = cleanedRoadCondition.charAt(i);
                if (roadChar == '■'
                        || roadChar == '1') {
                    created.addCar(new Car(this), i);
                }
            }
            roads.add(created);
        }
        return created;
    }

    /**
     * 指定した道路を管理対象から外す.
     *
     * @param road 管理対象から外す道路
     */
    public void eraceRoad(Road road) {
        roads.remove(road);
    }

    /**
     * すべての道路を管理対象から外す.
     */
    public void clear() {
        roads.clear();
    }

    /**
     * 管理している道すべての車を動かす.
     */
    public void update() {
        for (Road road : roads) {
            road.update(rule);
        }
    }

    /**
     * 指定した車道の右車線を返す.
     *
     * @param road 指定した車道
     * @return 右車線
     */
    @Nullable
    Road right(Road road) {
        int index = roads.indexOf(road);

        if (index == -1) {
            throw new IllegalArgumentException("Road not managed by this manager.");
        }

        try {
            return roads.get(index + 1);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * 指定した車道の左車線を返す.
     *
     * @param road 指定した車道
     * @return 右左車線
     */
    @Nullable
    Road left(Road road) {
        int index = roads.indexOf(road);

        if (index == -1) {
            throw new IllegalArgumentException("Road not managed by this manager.");
        }

        try {
            return roads.get(index - 1);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * このTrafficManagerが管理する車全体に適用される移動ルールを設定する.
     * 道路または車固有のルールのほうが優先される.
     *
     * @param rule ルール
     */
    public void addRule(Car.Rule rule) {
        this.rule = rule;
    }

    /**
     * このTrafficManagerが管理する車全体に適用される移動ルールを削除する.
     * 道路または車固有のルールには影響しない.
     */
    public void removeRule() {
        rule = null;
    }

    /**
     * 管理しているすべての道のバッファを反映する.
     */
    public void reflesh() {
        for (Road road : roads) {
            road.reflesh();
        }
    }

    /**
     * 管理している道すべての車の速度を変更する.
     *
     * @param speed スピード
     */
    public void setSpeed(int speed) {
        for (Road road : roads) {
            road.setSpeed(speed);
        }
    }

    /**
     * 指定した車の走っている道を返す.
     * 管理対象外の車の場合はnullを返す.
     *
     * @param car 車
     * @return 走っている道 or null
     */
    @Nullable
    Road getRoad(Car car) {
        Road contain = null;

        for (Road road : roads) {
            if (road.contains(car)) {
                contain = road;
                break;
            }
        }

        return contain;
    }

    /**
     * 車を指定した長さ進める.
     *
     * @param target 車
     * @param length 進める長さ. 移動可能な長さよりも長い場合, 移動可能な長さに丸められる.
     */
    void moveCar(Car target, int length) {
        Road road = getRoad(target);
        if (road == null) {
            throw new IllegalArgumentException("Car not managed by this manager.");
        }

        if (length < 0)
            throw new IllegalArgumentException("length must be bigger than or equal to 0.");

        if (length > road.forwardFreeLength(target))
            length = road.forwardFreeLength(target);

        int currentPosition = road.positionOf(target);

        road.removeCar(target);
        road.addCar(target, currentPosition + length);
    }

    /**
     * 指定した車の車線を変更する.
     * 今回は使用せず.
     *
     * @param target 車
     * @param to     移動先の道
     * @return 成功／失敗
     */
    boolean changeRoad(Car target, Road to) {
        if (!roads.contains(to)) {
            throw new IllegalArgumentException("Road not managed by this manager.");
        }

        Road from = getRoad(target);
        if (from == null) {
            return false;
        }

        if (to.addCar(target, from.positionOf(target))) {
            from.removeCar(target);
            return true;
        }

        return false;
    }
}
